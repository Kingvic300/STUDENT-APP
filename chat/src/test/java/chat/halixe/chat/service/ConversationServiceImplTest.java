package chat.halixe.chat.service;

import chat.halixe.chat.data.enums.ConversationRole;
import chat.halixe.chat.data.enums.ConversationType;
import chat.halixe.chat.data.model.Conversation;
import chat.halixe.chat.data.model.ConversationMember;
import chat.halixe.chat.data.model.ConversationSettings;
import chat.halixe.chat.data.repository.ConversationMemberRepository;
import chat.halixe.chat.data.repository.ConversationRepository;
import chat.halixe.chat.data.repository.ConversationSettingsRepository;
import chat.halixe.chat.dtos.request.AddConversationMemberRequest;
import chat.halixe.chat.dtos.request.CreateConversationRequest;
import chat.halixe.chat.dtos.response.ConversationMemberResponse;
import chat.halixe.chat.dtos.response.ConversationResponse;
import chat.halixe.chat.exception.ConversationNotFoundException;
import chat.halixe.chat.exception.ForbiddenException;
import chat.halixe.chat.exception.MemberAlreadyExistsException;
import chat.halixe.chat.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private ConversationMemberRepository memberRepository;
    @Mock private ConversationSettingsRepository settingsRepository;

    @InjectMocks private ConversationServiceImpl service;

    private static final UUID CONV_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private AuthenticatedUser caller() {
        return new AuthenticatedUser(USER_ID, ORG_ID);
    }


    @Test
    void createConversationSavesConversationAndMembers() {
        CreateConversationRequest request = new CreateConversationRequest();
        request.setType(ConversationType.CHANNEL);
        request.setName("general");

        Conversation saved = Conversation.builder()
                .id(CONV_ID).type(ConversationType.CHANNEL)
                .organisationId(ORG_ID).name("general").createdBy(USER_ID).build();

        when(conversationRepository.save(any())).thenReturn(saved);
        when(settingsRepository.save(any())).thenReturn(new ConversationSettings());
        when(memberRepository.saveAll(any())).thenReturn(List.of());

        ConversationResponse response = service.createConversation(request, caller());

        assertThat(response.getId()).isEqualTo(CONV_ID);
        assertThat(response.getName()).isEqualTo("general");
        verify(memberRepository).saveAll(any());
        verify(settingsRepository).save(any());
    }

    @Test
    void createConversationAddsExtraMembers() {
        UUID memberId = UUID.randomUUID();
        CreateConversationRequest request = new CreateConversationRequest();
        request.setType(ConversationType.DIRECT);
        request.setName("dm");
        request.setMembersId(List.of(memberId));

        Conversation saved = Conversation.builder()
                .id(CONV_ID).type(ConversationType.DIRECT)
                .organisationId(ORG_ID).name("dm").createdBy(USER_ID).build();

        when(conversationRepository.save(any())).thenReturn(saved);
        when(settingsRepository.save(any())).thenReturn(new ConversationSettings());
        when(memberRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createConversation(request, caller());

        verify(memberRepository).saveAll(argThat(list -> {
            List<?> l = (List<?>) list;
            return l.size() == 2;
        }));
    }

    @Test
    void getConversationReturnsResponse() {
        Conversation conv = Conversation.builder()
                .id(CONV_ID).organisationId(ORG_ID).name("general")
                .type(ConversationType.CHANNEL).build();

        when(conversationRepository.findByIdAndOrganisationId(CONV_ID, ORG_ID))
                .thenReturn(Optional.of(conv));
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, USER_ID))
                .thenReturn(true);
        when(memberRepository.countByConversationIdAndLeftAtIsNull(CONV_ID)).thenReturn(3L);

        ConversationResponse response = service.getConversation(CONV_ID, caller());

        assertThat(response.getId()).isEqualTo(CONV_ID);
        assertThat(response.getMemberCount()).isEqualTo(3L);
    }

    @Test
    void getConversationThrowsWhenNotFound() {
        when(conversationRepository.findByIdAndOrganisationId(CONV_ID, ORG_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getConversation(CONV_ID, caller()))
                .isInstanceOf(ConversationNotFoundException.class);
    }

    @Test
    void getConversationThrowsWhenCallerNotMember() {
        Conversation conv = Conversation.builder().id(CONV_ID).organisationId(ORG_ID).build();
        when(conversationRepository.findByIdAndOrganisationId(CONV_ID, ORG_ID))
                .thenReturn(Optional.of(conv));
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, USER_ID))
                .thenReturn(false);

        assertThatThrownBy(() -> service.getConversation(CONV_ID, caller()))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── addMember ──────────────────────────────────────────────────────────

    @Test
    void addMemberSavesNewMember() {
        UUID newUser = UUID.randomUUID();
        AddConversationMemberRequest request = new AddConversationMemberRequest();
        request.setUserId(newUser);
        request.setRole(ConversationRole.MEMBER);

        ConversationMember adminMember = ConversationMember.builder()
                .conversationId(CONV_ID).userId(USER_ID).role(ConversationRole.ADMIN).build();

        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);
        when(memberRepository.findByConversationIdAndUserId(CONV_ID, USER_ID))
                .thenReturn(Optional.of(adminMember));
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, newUser))
                .thenReturn(false);
        ConversationMember savedMember = ConversationMember.builder()
                .id(UUID.randomUUID()).conversationId(CONV_ID).userId(newUser)
                .role(ConversationRole.MEMBER).build();
        when(memberRepository.save(any())).thenReturn(savedMember);

        ConversationMemberResponse response = service.addMember(CONV_ID, request, caller());

        assertThat(response.getUserId()).isEqualTo(newUser);
        assertThat(response.getRole()).isEqualTo(ConversationRole.MEMBER);
    }

    @Test
    void addMemberThrowsWhenAlreadyMember() {
        UUID newUser = UUID.randomUUID();
        AddConversationMemberRequest request = new AddConversationMemberRequest();
        request.setUserId(newUser);

        ConversationMember adminMember = ConversationMember.builder()
                .conversationId(CONV_ID).userId(USER_ID).role(ConversationRole.ADMIN).build();

        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);
        when(memberRepository.findByConversationIdAndUserId(CONV_ID, USER_ID))
                .thenReturn(Optional.of(adminMember));
        when(memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(CONV_ID, newUser))
                .thenReturn(true);

        assertThatThrownBy(() -> service.addMember(CONV_ID, request, caller()))
                .isInstanceOf(MemberAlreadyExistsException.class);
    }

    @Test
    void addMemberThrowsWhenCallerIsPlainMember() {
        ConversationMember plainMember = ConversationMember.builder()
                .conversationId(CONV_ID).userId(USER_ID).role(ConversationRole.MEMBER).build();

        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);
        when(memberRepository.findByConversationIdAndUserId(CONV_ID, USER_ID))
                .thenReturn(Optional.of(plainMember));

        assertThatThrownBy(() -> service.addMember(CONV_ID, new AddConversationMemberRequest(), caller()))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── promoteMember / demoteMember ───────────────────────────────────────

    @Test
    void promoteMemberSetsAdminRole() {
        UUID targetUser = UUID.randomUUID();
        ConversationMember adminCaller = ConversationMember.builder()
                .conversationId(CONV_ID).userId(USER_ID).role(ConversationRole.ADMIN).build();
        ConversationMember target = ConversationMember.builder()
                .id(UUID.randomUUID()).conversationId(CONV_ID).userId(targetUser)
                .role(ConversationRole.MEMBER).build();

        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);
        when(memberRepository.findByConversationIdAndUserId(CONV_ID, USER_ID))
                .thenReturn(Optional.of(adminCaller));
        when(memberRepository.findByConversationIdAndUserId(CONV_ID, targetUser))
                .thenReturn(Optional.of(target));
        when(memberRepository.save(target)).thenReturn(target);

        ConversationMemberResponse response = service.promoteMember(CONV_ID, targetUser, caller());

        assertThat(response.getRole()).isEqualTo(ConversationRole.ADMIN);
    }

    @Test
    void demoteMemberSetsMemberRole() {
        UUID targetUser = UUID.randomUUID();
        ConversationMember ownerCaller = ConversationMember.builder()
                .conversationId(CONV_ID).userId(USER_ID).role(ConversationRole.OWNER).build();
        ConversationMember target = ConversationMember.builder()
                .id(UUID.randomUUID()).conversationId(CONV_ID).userId(targetUser)
                .role(ConversationRole.ADMIN).build();

        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);
        when(memberRepository.findByConversationIdAndUserId(CONV_ID, USER_ID))
                .thenReturn(Optional.of(ownerCaller));
        when(memberRepository.findByConversationIdAndUserId(CONV_ID, targetUser))
                .thenReturn(Optional.of(target));
        when(memberRepository.save(target)).thenReturn(target);

        ConversationMemberResponse response = service.demoteMember(CONV_ID, targetUser, caller());

        assertThat(response.getRole()).isEqualTo(ConversationRole.MEMBER);
    }

    // ── removeMember / leaveConversation ───────────────────────────────────

    @Test
    void removeMemberCallsMarkLeft() {
        UUID targetUser = UUID.randomUUID();
        ConversationMember adminCaller = ConversationMember.builder()
                .conversationId(CONV_ID).userId(USER_ID).role(ConversationRole.ADMIN).build();

        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);
        when(memberRepository.findByConversationIdAndUserId(CONV_ID, USER_ID))
                .thenReturn(Optional.of(adminCaller));

        service.removeMember(CONV_ID, targetUser, caller());

        verify(memberRepository).markLeft(eq(CONV_ID), eq(targetUser), any());
    }

    @Test
    void leaveConversationCallsMarkLeftForCaller() {
        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);

        service.leaveConversation(CONV_ID, caller());

        verify(memberRepository).markLeft(eq(CONV_ID), eq(USER_ID), any());
    }

    // ── muteConversation ───────────────────────────────────────────────────

    @Test
    void muteConversationDelegatesToRepository() {
        when(conversationRepository.existsByIdAndOrganisationId(CONV_ID, ORG_ID)).thenReturn(true);

        service.muteConversation(CONV_ID, true, caller());

        verify(memberRepository).updateMuted(CONV_ID, USER_ID, true);
    }
}

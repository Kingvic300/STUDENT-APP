package chat.halixe.chat.service;

import chat.halixe.chat.data.enums.ConversationRole;
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
import chat.halixe.chat.mappers.ConversationMapper;
import chat.halixe.chat.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final ConversationSettingsRepository settingsRepository;

    @Override
    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest request, AuthenticatedUser caller) {
      Conversation conversation = ConversationMapper.createConversation(request, caller);

        conversation = conversationRepository.save(conversation);

        ConversationSettings settings = ConversationMapper.createConversationSettings(conversation);

        settingsRepository.save(settings);

        List<ConversationMember> members = new ArrayList<>();

        members.add(ConversationMapper.createConversationMember(conversation, caller));

        if (request.getMembersId() != null) {
            for (UUID memberId : request.getMembersId()) {
                if (!memberId.equals(caller.userId())) {
                    members.add(ConversationMapper.addConversationMember(conversation, memberId));
                }
            }
        }

        memberRepository.saveAll(members);

        long memberCount = members.size();
        return ConversationMapper.toResponse(conversation, memberCount);
    }

    @Override
    public ConversationResponse getConversation(UUID conversationId, AuthenticatedUser caller) {
        Conversation conversation =
                conversationRepository
                        .findByIdAndOrganisationId(
                                conversationId,
                                caller.organisationId()
                        )
                        .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));

        requireMember(conversationId, caller.userId());

        long memberCount = memberRepository.countByConversationIdAndLeftAtIsNull(conversationId);
        return ConversationMapper.toResponse(conversation, memberCount);
    }

    @Override
    public Page<ConversationResponse> listConversations(Pageable pageable, AuthenticatedUser caller) {
        Set<UUID> conversationIds = memberRepository
                .findByUserIdAndLeftAtIsNull(caller.userId())
                .stream()
                .map(ConversationMember::getConversationId)
                .collect(Collectors.toSet());

        if (conversationIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return conversationRepository
                .findByOrganisationIdAndIdIn(caller.organisationId(), conversationIds, pageable)
                .map(c -> ConversationMapper.toResponse(c, memberRepository.countByConversationIdAndLeftAtIsNull(c.getId())));
    }

    @Override
    @Transactional
    public ConversationMemberResponse addMember(
            UUID conversationId,
            AddConversationMemberRequest request,
            AuthenticatedUser caller
    ) {
        requireConversationInOrg(conversationId, caller.organisationId());
        requireAdminOrOwner(conversationId, caller.userId());

        if (memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(conversationId, request.getUserId())) {
            throw new MemberAlreadyExistsException("User is already a member of this conversation");
        }

        ConversationRole role ;
        if (request.getRole() != null) {
            role = request.getRole();
        }else {
            role = ConversationRole.MEMBER;
        }
        ConversationMember member = ConversationMapper.addConversationMember(conversationId, request.getUserId(), role);

        member = memberRepository.save(member);
        return toMemberResponse(member);
    }

    @Override
    @Transactional
    public void removeMember(UUID conversationId, UUID userId, AuthenticatedUser caller) {
        requireConversationInOrg(conversationId, caller.organisationId());
        requireAdminOrOwner(conversationId, caller.userId());

        memberRepository.markLeft(conversationId, userId, Instant.now());
    }

    @Override
    @Transactional
    public void leaveConversation(UUID conversationId, AuthenticatedUser caller) {
        requireConversationInOrg(conversationId, caller.organisationId());
        memberRepository.markLeft(conversationId, caller.userId(), Instant.now());
    }

    @Override
    public List<ConversationMemberResponse> listMembers(UUID conversationId, AuthenticatedUser caller) {
        requireConversationInOrg(conversationId, caller.organisationId());
        requireMember(conversationId, caller.userId());

        return memberRepository.findByConversationIdAndLeftAtIsNull(conversationId)
                .stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Override
    @Transactional
    public ConversationMemberResponse promoteMember(UUID conversationId, UUID userId, AuthenticatedUser caller) {
        requireConversationInOrg(conversationId, caller.organisationId());
        requireAdminOrOwner(conversationId, caller.userId());

        ConversationMember member = memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ForbiddenException("User is not a member of this conversation"));

        member.setRole(ConversationRole.ADMIN);
        member = memberRepository.save(member);
        return toMemberResponse(member);
    }

    @Override
    @Transactional
    public ConversationMemberResponse demoteMember(UUID conversationId, UUID userId, AuthenticatedUser caller) {
        requireConversationInOrg(conversationId, caller.organisationId());
        requireAdminOrOwner(conversationId, caller.userId());

        ConversationMember member = memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ForbiddenException("User is not a member of this conversation"));

        member.setRole(ConversationRole.MEMBER);
        member = memberRepository.save(member);
        return toMemberResponse(member);
    }

    @Override
    @Transactional
    public void muteConversation(UUID conversationId, boolean muted, AuthenticatedUser caller) {
        requireConversationInOrg(conversationId, caller.organisationId());
        memberRepository.updateMuted(conversationId, caller.userId(), muted);
    }

    private void requireConversationInOrg(UUID conversationId, UUID organisationId) {
        if (!conversationRepository.existsByIdAndOrganisationId(conversationId, organisationId)) {
            throw new ConversationNotFoundException("Conversation not found: " + conversationId);
        }
    }

    private void requireMember(UUID conversationId, UUID userId) {
        if (!memberRepository.existsByConversationIdAndUserIdAndLeftAtIsNull(conversationId, userId)) {
            throw new ForbiddenException("You are not a member of this conversation");
        }
    }

    private void requireAdminOrOwner(UUID conversationId, UUID userId) {
        ConversationMember member = memberRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this conversation"));

        if (member.getRole() == ConversationRole.MEMBER || member.getLeftAt() != null) {
            throw new ForbiddenException("Insufficient permissions");
        }
    }

    private ConversationMemberResponse toMemberResponse(ConversationMember m) {
        return ConversationMapper.toMemberResponse(m);
    }
}

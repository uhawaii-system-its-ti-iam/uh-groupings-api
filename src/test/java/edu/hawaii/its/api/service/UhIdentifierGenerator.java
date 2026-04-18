package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.groupings.GroupingGroupMember;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;
import edu.hawaii.its.api.groupings.GroupingMember;
import edu.hawaii.its.api.groupings.GroupingMembers;

@PropertySource(value = "classpath:application-integrationTest.properties")
@Service("UhIdentifierGenerator")
public class UhIdentifierGenerator {

    private static final int PAGE_SIZE = 50;
    private static final int MIN_PAGE_NUMBER = 1;
    private static final int MAX_PAGE_NUMBER = 100;

    @Value("${groupings.api.test.grouping_large_basis}")
    private String GROUPING_LARGE_BASIS;

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    private final GroupingOwnerService groupingOwnerService;
    private final MemberService memberService;

    public UhIdentifierGenerator(GroupingOwnerService groupingOwnerService,
                                 MemberService memberService) {
        this.groupingOwnerService = groupingOwnerService;
        this.memberService = memberService;
    }

    public GroupingMember getRandomMember() {
        List<GroupingMember> members = getGroupingMembers(1);

        if (members.isEmpty()) {
            throw new IllegalStateException(
                    "Unable to retrieve a safe random member from grouping: " + GROUPING_LARGE_BASIS);
        }

        return members.get(getRandomNumberBetween(0, members.size() - 1));
    }

    public GroupingMembers getRandomMembers(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be greater than or equal to 0.");
        }

        if (amount == 0) {
            return new GroupingMembers(new ArrayList<>());
        }

        List<GroupingMember> members = getGroupingMembers(amount);

        if (members.size() < amount) {
            throw new IllegalStateException(
                    "Requested " + amount + " safe random members, but only found "
                            + members.size() + " safe members in grouping: " + GROUPING_LARGE_BASIS);
        }

        List<GroupingMember> randomMembers = new ArrayList<>();
        Set<String> selectedUids = new HashSet<>();

        while (randomMembers.size() < amount) {
            GroupingMember randomMember = members.get(getRandomNumberBetween(0, members.size() - 1));
            String uid = randomMember.getUid();

            if (selectedUids.add(uid)) {
                randomMembers.add(randomMember);
            }
        }

        return new GroupingMembers(randomMembers);
    }

    private List<GroupingMember> getGroupingMembers(int minimumAmount) {
        List<GroupingMember> members = new ArrayList<>();
        Set<String> seenUids = new HashSet<>();

        for (Integer pageNumber : getRandomizedPageNumbers()) {
            if (members.size() >= minimumAmount) {
                break;
            }

            List<GroupingMember> pageMembers = getGroupingMembersByPage(pageNumber);

            for (GroupingMember member : pageMembers) {
                if (isSafeTestMember(member) && seenUids.add(member.getUid())) {
                    members.add(member);
                }

                if (members.size() >= minimumAmount) {
                    break;
                }
            }
        }

        return members;
    }

    private List<Integer> getRandomizedPageNumbers() {
        List<Integer> pageNumbers = new ArrayList<>();

        for (int pageNumber = MIN_PAGE_NUMBER; pageNumber <= MAX_PAGE_NUMBER; pageNumber++) {
            pageNumbers.add(pageNumber);
        }

        Collections.shuffle(pageNumbers);
        return pageNumbers;
    }

    private boolean isSafeTestMember(GroupingMember member) {
        if (member == null || member.getUid() == null || member.getUid().isEmpty()) {
            return false;
        }

        String uid = member.getUid();

        return !memberService.isAdmin(uid)
                && !memberService.isOwner(GROUPING, uid)
                && !memberService.isMember(GROUPING_INCLUDE, uid)
                && !memberService.isMember(GROUPING_EXCLUDE, uid);
    }

    private List<GroupingMember> getGroupingMembersByPage(int pageNumber) {
        GroupingGroupMembers groupingGroupMembers = groupingOwnerService.getGroupingMembers(
                ADMIN,
                GROUPING_LARGE_BASIS,
                pageNumber,
                PAGE_SIZE,
                null,
                true
        );

        List<GroupingMember> members = new ArrayList<>();
        List<GroupingGroupMember> groupMembers = groupingGroupMembers.getMembers();

        if (groupMembers == null) {
            return members;
        }

        for (GroupingGroupMember groupMember : groupMembers) {
            members.add(new GroupingMember(groupMember, ""));
        }

        return members;
    }

    private int getRandomNumberBetween(int start, int end) {
        if (end < start) {
            throw new IllegalArgumentException(
                    "Invalid random range: start=" + start + ", end=" + end);
        }

        return ThreadLocalRandom.current().nextInt(start, end + 1);
    }
}
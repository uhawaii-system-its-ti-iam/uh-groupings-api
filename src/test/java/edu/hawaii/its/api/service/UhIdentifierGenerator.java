package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.groupings.GroupingMember;
import edu.hawaii.its.api.groupings.GroupingMembers;

@PropertySource(value = "classpath:application-integrationTest.properties")
@Service("UhIdentifierGenerator")
public class UhIdentifierGenerator {

    @Value("${groupings.api.test.grouping_large_basis}")
    private String GROUPING_LARGE_BASIS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    private final GroupingOwnerService groupingOwnerService;

    public UhIdentifierGenerator(GroupingOwnerService groupingOwnerService) {
        this.groupingOwnerService = groupingOwnerService;
    }

    public GroupingMember getRandomMember() {
        List<GroupingMember> members = getGroupingMembers();
        GroupingMember randomMember;

        do {
            randomMember = members.get(getRandomNumberBetween(0, members.size() - 1));
        } while (randomMember.getUid().isEmpty() || isAdminUser(randomMember));

        return randomMember;
    }

    public GroupingMembers getRandomMembers(int amount) {
        List<GroupingMember> members = getGroupingMembers();
        HashSet<GroupingMember> randomMembers = new HashSet<>();

        long eligibleCount = members.stream()
                .filter(member -> !member.getUid().isEmpty())
                .filter(member -> !isAdminUser(member))
                .count();

        if (amount > eligibleCount) {
            throw new IllegalArgumentException("Requested random members exceeds eligible pool size");
        }

        while (randomMembers.size() != amount) {
            GroupingMember randomMember = members.get(getRandomNumberBetween(0, members.size() - 1));
            if (!randomMember.getUid().isEmpty() && !isAdminUser(randomMember)) {
                randomMembers.add(randomMember);
            }
        }

        return new GroupingMembers(new ArrayList<>(randomMembers));
    }

    private List<GroupingMember> getGroupingMembers() {
        return groupingOwnerService.paginatedGrouping(
                ADMIN,
                Collections.singletonList(GROUPING_LARGE_BASIS),
                getRandomNumberBetween(1, 100),
                50,
                null,
                true).getAllMembers().getMembers();
    }

    private int getRandomNumberBetween(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }

    private boolean isAdminUser(GroupingMember member) {
        return Objects.equals(member.getUid(), ADMIN);
    }

}

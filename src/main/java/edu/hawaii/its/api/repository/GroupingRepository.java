package edu.hawaii.its.api.repository;

import edu.hawaii.its.api.type.Grouping;

import org.springframework.data.repository.CrudRepository;

public interface GroupingRepository extends CrudRepository<Grouping, String> {
    Grouping findByPath(String path);

    Grouping findByIncludePathOrExcludePathOrCompositePathOrOwnersPath(String path0, String path1, String path2, String path3);

}

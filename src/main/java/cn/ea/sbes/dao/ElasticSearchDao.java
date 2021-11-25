package cn.ea.sbes.dao;

import cn.ea.sbes.pojo.D2;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

//@Repository
public interface ElasticSearchDao extends PagingAndSortingRepository<D2, Integer> {
}

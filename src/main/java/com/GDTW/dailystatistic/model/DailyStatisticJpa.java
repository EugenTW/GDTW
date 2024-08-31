package com.GDTW.dailystatistic.model;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface DailyStatisticJpa  extends JpaRepository<DailyStatisticVO, Integer> {

    DailyStatisticVO findByDsDate(Date dsDate);

}

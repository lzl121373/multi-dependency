package cn.edu.fudan.se.multidependency.service.coupling;

import cn.edu.fudan.se.multidependency.MultipleDependencyApp;
import cn.edu.fudan.se.multidependency.service.query.coupling.CouplingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = MultipleDependencyApp.class)
class CalGroupCouplingValueTest {
    @Autowired
    private final CouplingService couplingService;

    @Autowired
    public CalGroupCouplingValueTest(CouplingService couplingService) {
        this.couplingService = couplingService;
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void calGroupCouplingValue() throws IOException {
//        long[] fileIdList = {337,336,335,334,333,330,329,328,327,326,325};
        long[] fileIdList = {19001,19002,19003,19004,19005,19006,19007,19008,19009,19010,19011};
//        long[] fileIdList = {5988,5985,5986,5989,5977,5978,5979,5980,5981,5987,5982};
        List<Long> idList = new ArrayList<>();

        for(long fileId : fileIdList){
            idList.add(fileId);
        }
//        couplingService.calGroupCouplingValue(idList, "/Users/fulco/Desktop/Data/FDUPro/coupling/before.csv");
        couplingService.calGroupInstablity(idList);
    }
}
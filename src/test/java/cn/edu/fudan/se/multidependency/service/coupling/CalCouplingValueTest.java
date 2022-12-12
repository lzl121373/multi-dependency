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


@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = MultipleDependencyApp.class)
class CalCouplingValueTest {
    @Autowired
    private final CouplingService couplingService;

    @Autowired
    public CalCouplingValueTest(CouplingService couplingService) {
        this.couplingService = couplingService;
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void calCouplingValue() throws IOException {
//        couplingService.calCouplingValue("/Users/fulco/Desktop/Data/FDUPro/coupling/coupling_value_export.csv");
    }
}
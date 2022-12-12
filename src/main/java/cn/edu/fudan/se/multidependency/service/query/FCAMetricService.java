package cn.edu.fudan.se.multidependency.service.query;

import java.io.IOException;

public interface FCAMetricService {
    void getFCAPackageDependsOnMetric() throws IOException;

    void getFCAFileDependsOnMetric() throws IOException;
}

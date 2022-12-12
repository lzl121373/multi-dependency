package cn.edu.fudan.se.multidependency.service.query.ar;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

@Service
public class DependencyMatrix {

    @Getter
    private Map<String, Map<String, Double>> adjacencyList;

    @Getter
    private Map<String, Set<String>> staticDependGraph;

    @Getter
    private Map<String, Set<String>> dynamicDependGraph;

    @Getter
    private Map<String, Set<String>> cochangeDependGraph;

    @Autowired
    private ProjectFileRepository fileRepository;

    @Getter
    private List<ProjectFile> projectFiles;

    //静态
    private static final double IMPORT_BTW_FILE = 1.0;
    private static final double INCLUDE_BTW_FILE = 1.0;
    private static final double EXTENDS_BTW_TYPE = 3.0;
    private static final double IMPLEMENTS_BTW_TYPE = 3.0;
    private static final double IMPLEMENTS_BTW_FUNC = 3.0;
    private static final double CREATE_BTW_TYPE = 1.0;
    private static final double CREATE_FROM_FUNC_TO_TYPE = 1.0;
    private static final double IMPLLINK_BTW_FUNC = 3.0;
    private static final double CALL_BTW_FUNC = 1.0;
    private static final double CALL_FROM_TYPE_TO_FUNC = 1.0;
    private static final double ACCESS_FROM_FUNC_TO_VAR = 1.0;
    private static final double MEMBER_VAR_TYPE_FROM_VAR_TO_TYPE = 1.0;
    private static final double LOCAL_VAR_TYPE_FROM_VAR_TO_TYPE = 1.0;
    private static final double PARA_From_Var_To_Type = 1.0;
    private static final double RETURN_FROM_FUNC_TO_TYPE = 1.0;
    private static final double THROW_FROM_FUNC_TO_TYPE = 1.0;
    private static final double CAST_FROM_FUNC_TO_TYPE = 1.0;

    //动态
    private static final double DYNAMIC_CALL_BTW_FUNC = 1.0;

    //演化
    private static final double CO_CHANGE = 1.0;

    private int flag = 0;
    private ProjectFile from;
    Map<String, Double> edges;

    private boolean isLoaded = false;

    public void init() {
        if (!isLoaded) {
            adjacencyList = new HashMap<>();
            staticDependGraph = new HashMap<>();
            dynamicDependGraph = new HashMap<>();
            cochangeDependGraph = new HashMap<>();
            projectFiles = (List<ProjectFile>) fileRepository.findAll();
            initGraphNodes();
            calculate();
            isLoaded = true;
        }
    }

    private void initGraphNodes() {
        for (ProjectFile projectFile : projectFiles) {
            adjacencyList.put(projectFile.getPath(), new HashMap<>());
            staticDependGraph.put(projectFile.getPath(), new HashSet<>());
            dynamicDependGraph.put(projectFile.getPath(), new HashSet<>());
            cochangeDependGraph.put(projectFile.getPath(), new HashSet<>());
        }
    }

    private void calculate() {
        for (ProjectFile projectFile : projectFiles) {
            this.from = projectFile;
            this.edges = adjacencyList.get(from.getPath());

            //静态
            this.flag = 0;
            Long fromId = from.getId();
            calculate(fileRepository.getImportBtwFile(fromId), IMPORT_BTW_FILE);
            calculate(fileRepository.getIncludeBtwFile(fromId), INCLUDE_BTW_FILE);
            calculate(fileRepository.getExtendBtwType(fromId), EXTENDS_BTW_TYPE);
            calculate(fileRepository.getImplementBtwType(fromId), IMPLEMENTS_BTW_TYPE);
            calculate(fileRepository.getImplementBtwFunc(fromId), IMPLEMENTS_BTW_FUNC);
            calculate(fileRepository.getCreateBtwType(fromId), CREATE_BTW_TYPE);
            calculate(fileRepository.getCreateFromFuncToType(fromId), CREATE_FROM_FUNC_TO_TYPE);
            calculate(fileRepository.getImpllinkBtwFunc(fromId), IMPLLINK_BTW_FUNC);
            calculate(fileRepository.getCallBtwFunc(fromId), CALL_BTW_FUNC);
            calculate(fileRepository.getCallFromTypeToFunc(fromId), CALL_FROM_TYPE_TO_FUNC);
            calculate(fileRepository.getAccessFromFuncToVar(fromId), ACCESS_FROM_FUNC_TO_VAR);
            calculate(fileRepository.getMemberVarTypeFromVarToType(fromId), MEMBER_VAR_TYPE_FROM_VAR_TO_TYPE);
            calculate(fileRepository.getLocalVarTypeFromVarToType(fromId), LOCAL_VAR_TYPE_FROM_VAR_TO_TYPE);
            calculate(fileRepository.getParaFromVarToType(fromId), PARA_From_Var_To_Type);
            calculate(fileRepository.getReturnFromFuncToType(fromId), RETURN_FROM_FUNC_TO_TYPE);
            calculate(fileRepository.getThrowFromFuncToType(fromId), THROW_FROM_FUNC_TO_TYPE);
            calculate(fileRepository.getCastFromFuncToType(fromId), CAST_FROM_FUNC_TO_TYPE);

            //动态
//            this.flag = 1;
//            calculate(fileRepository.getDynamicCallBtwFunc(fromId), DYNAMIC_CALL_BTW_FUNC);

            //演化
//            this.flag = 2;
//            calculate(fileRepository.getCoChangeFiles(fromId), CO_CHANGE);
        }
    }

    private void calculate(List<DependencyPair> pairs, double weight) {
        if (pairs == null || pairs.size() == 0) return;
        for (DependencyPair pair : pairs) {
            ProjectFile to = pair.getProjectFile();
            double val = weight * pair.getCount() / from.getEndLine();
            edges.put(to.getPath(), edges.getOrDefault(to.getPath(), 0.0) + val);
            switch (this.flag) {
                case 0:
                    staticDependGraph.get(from.getPath()).add(to.getPath());
                    break;
                case 1:
                    dynamicDependGraph.get(from.getPath()).add(to.getPath());
                    break;
                case 2:
                    cochangeDependGraph.get(from.getPath()).add(to.getPath());
                    Map<String, Double> tmp = adjacencyList.get(to.getPath());
                    tmp.put(from.getPath(), tmp.getOrDefault(from.getPath(), 0.0) + val);
                    cochangeDependGraph.get(to.getPath()).add(from.getPath());
            }
        }
    }

    public void exportMatrix(String filePath, Map<String, Map<String, Double>> map) {
        StringBuffer buf = new StringBuffer();
        DecimalFormat df = new DecimalFormat("0.00000");
        for (String file1 : map.keySet()) {
            buf.append(file1 + "\n");
            for (Map.Entry<String, Double> entry : map.get(file1).entrySet()) {
                buf.append("-----" + entry.getKey() + ":" + df.format(entry.getValue()) + "\n");
            }
        }
        FileUtil.exportToFile(filePath, buf.toString());
    }

}

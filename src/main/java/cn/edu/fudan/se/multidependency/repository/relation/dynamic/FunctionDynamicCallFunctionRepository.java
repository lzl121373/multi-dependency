package cn.edu.fudan.se.multidependency.repository.relation.dynamic;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCall;
import cn.edu.fudan.se.multidependency.model.relation.structure.Call;

@Repository
public interface FunctionDynamicCallFunctionRepository extends Neo4jRepository<DynamicCall, Long> {

	/**
	 * 找出给定id的方法动态调用了哪些方法
	 * @param id
	 * @return
	 */
	@Query("match p = (a:Function)-[:" + RelationType.str_DYNAMIC_CALL + "]-(b:Function) where id(a) = $0 return b")
    List<Function> findCallFunctions(Long id);

	@Query("match p = ()-[r:" + RelationType.str_DYNAMIC_CALL + "]->() where r.traceId=$traceId and r.spanId=$spanId return p")
	List<DynamicCall> findFunctionCallsByTraceIdAndSpanId(@Param("traceId") String traceId, @Param("spanId") String spanId);

	@Query("match p = ()-[r:" + RelationType.str_DYNAMIC_CALL + "]->() where r.traceId=$traceId return p")
	List<DynamicCall> findFunctionCallsByTraceId(@Param("traceId") String traceId);

	@Query("match p = ()-[r:" + RelationType.str_DYNAMIC_CALL + "]->() where r.projectName=$projectName and r.language=$language return p")
	List<DynamicCall> findFunctionCallsByProjectNameAndLanguage(@Param("projectName") String projectName, @Param("language") String language);
	
	@Query("MATCH result=(function1:Function)-[:" + RelationType.str_DYNAMIC_CALL + "]->(function2:Function) with function1,function2,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*3..5]->(function1) where id(project)=$projectId RETURN result")
	public List<DynamicCall> findProjectContainFunctionDynamicCallFunctionRelations(@Param("projectId") Long projectId);

	@Query("match result = (f1:Function)-[:" + RelationType.str_CALL + "]->(f2:Function) where not (f1)-[:" + RelationType.str_DYNAMIC_CALL + "{testCaseId:$testcaseId}]->(f2) return result")
	public List<Call> findFunctionCallFunctionNotDynamicCalled(@Param("testcaseId") Integer testcaseId);
	
	@Query("match result = (f1:Function)-[:" + RelationType.str_CALL + "]->(f2:Function) where (f1)-[:" + RelationType.str_DYNAMIC_CALL + "{testCaseId:$testcaseId}]->(f2) return result")
	public List<Call> findFunctionCallFunctionDynamicCalled(@Param("testcaseId") Integer testcaseId);
	
	@Query("match result = (f1:Function)-[:" + RelationType.str_CALL + "]->(f2:Function) where not (f1)-[:" + RelationType.str_DYNAMIC_CALL + "]->(f2) return result")
	public List<Call> findFunctionCallFunctionNotDynamicCalled();
	
	@Query("MATCH result=(function1:Function)-[:" + RelationType.str_DYNAMIC_CALL + "]->(function2:Function) where id(function1)=$functionId return result")
	public List<DynamicCall> findDynamicCallsByCallerId(@Param("functionId") Long callerFunctionId);
	
	@Query("MATCH result=(function1:Function)-[r:" + RelationType.str_DYNAMIC_CALL + "]->(function2:Function) where id(function1)=$functionId and r.testCaseId=$testCaseId return result")
	public List<DynamicCall> findDynamicCallsByCallerIdAndTestCaseId(@Param("functionId") Long callerFunctionId, @Param("testCaseId") Integer testCaseId);
	
	@Query("MATCH result=(function1:Function)-[:" + RelationType.str_DYNAMIC_CALL + "]->(function2:Function) where id(function1)=$callerId1 and id(function2)=$calledId return result")
	public List<DynamicCall> findDynamicCallsByCallerIdAndCalledId(@Param("callerId") Long functionCallerId, @Param("calledId") Long functionCalledId);
	
	@Query("MATCH result=(function1:Function)-[r:" + RelationType.str_DYNAMIC_CALL + "]->(function2:Function) where id(function1)=$callerId1 and id(function2)=$calledId and r.testCaseId=$testCaseId return result")
	public List<DynamicCall> findDynamicCallsByCallerIdAndCalledIdAndTestCaseId(@Param("callerId") Long functionCallerId, @Param("calledId") Long functionCalledId, @Param("testCaseId") Integer testCaseId);
	
}

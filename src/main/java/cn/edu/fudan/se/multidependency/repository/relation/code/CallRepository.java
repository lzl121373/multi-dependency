package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.Call;

@Repository
public interface CallRepository extends Neo4jRepository<Call, Long> {
	
	@Query("MATCH result=(function1:Function)-[:" + RelationType.str_CALL + "]->(function2:Function) with function1,function2,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*3..5]->(function1) where id(project)=$projectId RETURN result")
	List<Call> findProjectContainFunctionCallFunctionRelations(@Param("projectId") Long projectId);
	
	/**
	 * 调用了哪些function
	 * @param functionId
	 * @return
	 */
	@Query("MATCH result=(function1:Function)-[:" + RelationType.str_CALL + "]->(function2:Function) where id(function1)=$functionId RETURN result")
	List<Call> queryFunctionCallFunctions(@Param("functionId") long functionId);

	/**
	 * function被哪些调用
	 * @param functionId
	 * @return
	 */
	@Query("MATCH result=(function1:Function)-[:" + RelationType.str_CALL + "]->(function2:Function) where id(function2)=$functionId RETURN result")
	List<Call> queryFunctionCallByFunctions(@Param("functionId") long functionId);
	
//	@Query("match (file1:ProjectFile)-[:CONTAIN*2]->(f:Function)-[r:FUNCTION_CALL_FUNCTION]->(function2:Function)<-[:CONTAIN*2]-(file2:ProjectFile) where id(file1)=$fileId and id(file1) <> id(file2) return r,file1,file2")
//	Object queryTest(@Param("fileId") long fileId);
	
	@Query("MATCH result=(type:Type)-[:" + RelationType.str_CALL + "]->(function:Function) with type,function,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*3..4]->(type) where id(project)=$projectId RETURN result")
	List<Call> findProjectContainTypeCallFunctionRelations(@Param("projectId") Long projectId);

	@Query("MATCH result=(function1:Function)-[:" + RelationType.str_CALL + "]->(function2:Function) RETURN result")
	List<Call> findAllFunctionCallFunctionRelations();

	@Query("match result=(function:Function)-[:" + RelationType.str_CALL + "]-(:Function) where id(function) = $functionId return result")
	List<Call> findFunctionContainCalls(@Param("functionId") long functionId);
}

###总结：
####1、实体行数计算上不止计算起始行数还计算了终止行数以及有效行数
####2、VarEntity支持类型名属性
####3、支持分析时排除部分文件或文件夹
####4、分析C++项目时分析所有头文件
####5、解析JAV项目时为了解决有同名包时关系会指向MultiDeclareEntities的问题，引入了新的RelationCounter，可以爱建立关系实体时通过距离来重新定向
####6、解决某些AliasEntity无法指向正确的实体的问题


###部分代码：
#####1、VarEntity类新增变量typeIdentifier变量，为变量实体增加类型名属性；在FprmalParameterListContextHelper类内增加遍历设置VarEntity的方法
```java
    private String typeIdentifier;
```   
#####2、CdtCppFileParser新增在解析时获取entity的有效代码行数
```java
    try {
        Method method = tu.getFileLocation().getClass().getMethod("getSource");
        method.setAccessible(true);
        char[] charArray = (char[]) method.invoke(tu.getFileLocation());
        fileEntity.setLoc(LocCalculator.calcLoc(new String(charArray)));
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        e.printStackTrace();
    }
```
#####3、CppVisitor新增设置entity的开始结束行数
```java
	private void setLineNumber(Entity entity, IASTNode node) {
		entity.setStartLine(node.getFileLocation().getStartingLineNumber());
		entity.setEndLine(node.getFileLocation().getEndingLineNumber());
	}
```
```java
	public int visit(IASTExpression expression) {
		if (notLocalFile(expression)) return ASTVisitor.PROCESS_SKIP;
		Expression expr = expressionUsage.foundExpression(expression);
		expr.setStartLine(expression.getFileLocation().getStartingLineNumber());
		expr.setEndLine(expression.getFileLocation().getEndingLineNumber());
		return super.visit(expression);
	}
```
#####4、JavaFileParser内新增解析时设置entity行数和有效行数
```java
	    try {
			JavaParser.CompilationUnitContext ctx = parser.compilationUnit();
			walker.walk(bridge, ctx);
			Entity fileEntity = entityRepo.getEntity(fileFullPath);
			((FileEntity)fileEntity).cacheAllExpressions();
			fileEntity.setEndLine(ctx.stop.getLine());
			fileEntity.setLoc(LocCalculator.calcLoc(input.toString()));
			interpreter.clearDFA();
			bridge.done();
	    }catch (Exception e) {
	    	System.err.println("error encountered during parse..." );
	    	e.printStackTrace();
	    }
```
#####5、JavaListener内新增获取typeentity和FunctionEntity起始终止行数
```java
		if (ctx.classBody().LBRACE() != null) {
			type.setStartLine(ctx.classBody().LBRACE().getSymbol().getLine());
		}
		if (ctx.classBody().RBRACE() != null) {
			type.setEndLine(ctx.classBody().RBRACE().getSymbol().getLine());
		}
```
#####6、AbstractLangProcessor、PreprocessorHandler增加excludePath属性支持分析时排除部分文件或文件夹
```java
    public List<String> excludePaths;
    fileTransversal.setExcludePaths(this.excludePaths);
```
#####7、HandlerContext类增加logger变量
```java
private static Logger logger = LoggerFactory.getLogger(HandlerContext.class);
```
#####8、可以分析所有C/C++头文件
```java
//	@Override
//	protected boolean isPhase2Files(String fileFullPath) {
//		if (fileFullPath.endsWith(".h") || fileFullPath.endsWith(".hh") || fileFullPath.endsWith(".hpp")
//				|| fileFullPath.endsWith(".hxx"))
//			return true;
//		return false;
//	}
```
#####9.增加计算代码有效行数的方法类LocCalculator







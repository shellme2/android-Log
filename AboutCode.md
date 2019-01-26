#日志库
----------
1. 目录结构     
    (rootDir)
    +-- app             -> 日志demo源码      
    |     
    |-- BfcLog-library                  -> 日志库的核心源码  对外提供的接口 及基本功能 都在这儿    
    |     
    |-- Bfclog-aop-library              -> 提供log方法耗时功能的代码  对注解的支持的代码 都在这儿     
    |    
    |-- BfcLog-plugin               -> 为Bfclog-aop-library和 注解 提供编译插件, 方便使用   
    |   
    +-- doc             -> 代码设计的相关文档   
        |   
        +-- 使用文档说明   
        |   
        +-- 设计文档   
        +-- ...      
        
2. 代码说明   
    aop极其注解 使用aspectJ实现   
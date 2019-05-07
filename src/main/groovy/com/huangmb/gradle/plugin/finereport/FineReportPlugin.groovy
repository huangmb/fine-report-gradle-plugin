package com.huangmb.gradle.plugin.finereport

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

class FineReportPlugin implements Plugin<ProjectInternal> {
    public static final String TASK_GROUP = "fine-report-plugin"
    public static final String TASK_PREPARE = "preparePlugin"
    public static final String TASK_COPY_RESOURCES = 'copyResources'
    public static final String TASK_ASSEMBLE_PLUGIN = 'assemblePlugin'
    public static final String TASK_COPY_LIBS = 'copyDownloadableDepsToLibs'
    public static final String TASK_BUNDLE_PLUGIN = 'bundlePlugin'

    public static final String REPORT_LIB_CONFIGURATION_NAME = "reportLib"

    public static final String FANRUAN_MAVEN = "http://mvn.finedevelop.com/repository/maven-public/"

    @Override
    void apply(ProjectInternal project) {
        if (!project.getPluginManager().hasPlugin("java")) {
            project.getPluginManager().apply('java')
            println "添加Java插件"
        }
        def fineReportExt = project.extensions.create("FineReportPlugin", FineReportPluginExt)
//        def pluginInfo = new XmlSlurper().parse("${project.projectDir}/plugin.xml")
        if (!fineReportExt.pluginName) {
            fineReportExt.pluginName = project.name//pluginInfo.name
        }
        if (!fineReportExt.pluginVersion) {
            fineReportExt.pluginVersion = project.version//pluginInfo.version
        }

        addFanruanRepositories(project)
        configureConfigurations(project)
        project.afterEvaluate {
            // 等FineReportPlugin配置初始化完毕
            addDependencies(project,fineReportExt)
            createTasks(project, fineReportExt)
        }



    }

    /**
     * 创建插件相关任务
     * @param project
     * @param fineExt
     */
    private void createTasks(Project project, FineReportPluginExt fineExt) {

        def pluginDir = "fr-plugin-${fineExt.pluginName}-${fineExt.pluginVersion}"
        def assemblePluginTask = project.tasks.create(TASK_ASSEMBLE_PLUGIN, Copy.class) {
            group TASK_GROUP
            description  "编译插件"

            doFirst {
                project.delete("${project.buildDir}/$pluginDir")
            }
            from "${project.projectDir}/plugin.xml"
            from "${project.buildDir}/libs"
            into "${project.buildDir}/$pluginDir"
        }
        def copyLibsTask = project.tasks.create(TASK_COPY_LIBS,Copy.class) {
            group TASK_GROUP
            description "复制非Report第三方依赖库"

            from project.configurations.compile
            into "${project.buildDir}/$pluginDir/lib"
        }
        def bundlePluginTask = project.tasks.create(TASK_BUNDLE_PLUGIN, Zip.class) {
            group TASK_GROUP
            description  "打包插件"
            archiveName "${pluginDir}.zip"
            destinationDir project.file("${project.buildDir}/install")

            from "${project.buildDir}/$pluginDir"
        }

        assemblePluginTask.dependsOn('jar')
        copyLibsTask.dependsOn(assemblePluginTask)
        bundlePluginTask.dependsOn(copyLibsTask)
    }

    /**
     * 增加一个名为reportLib的依赖配置项,用于标记fr相关包的依赖关系,继承自compileOnly
     * @param project
     */
    private void configureConfigurations(Project project) {
        def configurations = project.configurations
        def reportLibConfiguration = configurations.create(REPORT_LIB_CONFIGURATION_NAME)
        def compileOnly = configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)
        compileOnly.extendsFrom(reportLibConfiguration)
    }


    /**
     * 自动添加fr相关包依赖
     * @param project
     * @param reportExt
     */
    private void addDependencies(Project project, FineReportPluginExt reportExt) {
        def version = reportExt.coreVersion
        if (reportExt.enableSnapshot) {
            version += "-SNAPSHOT"
        }
        def reportLibs = [
                // core
                "com.fr.third:fine-third:$version",
                "com.fr.activator:fine-activator:$version",
                "com.fr.core:fine-core:$version",
                "com.fr.webui:fine-webui:$version",
                // 数据源
                "com.fr.datasource:fine-datasource:$version",
                // 决策平台
                "com.fr.decision:fine-decision:$version",
                "com.fr.decision:fine-decision-report:$version",
                // 定时调度
                "com.fr.schedule:fine-schedule:$version",
                "com.fr.schedule:fine-schedule-report:$version",
                // 智能日志
                "com.fr.intelligence:fine-accumulator:$version",
                // 报表引擎
                "com.fr.report:fine-report-engine:$version",
                // 设计器
                "com.fr.report:fine-report-designer:$version"
        ]
        reportLibs.each { lib ->
            project.dependencies.add(REPORT_LIB_CONFIGURATION_NAME, lib)
        }

        def others = [
                "io.socket:socket.io-client:0.7.0",
                "org.aspectj:aspectjrt:1.6.9",
                "org.swingexplorer:swexpl:2.0",
                "org.swingexplorer:swag:1.0",
                "org.apache.tomcat:tomcat-catalina:8.5.32",
                "mysql:mysql-connector-java:5.1.44"
        ]

        others.each { lib ->
            project.dependencies.add("compileOnly", lib)
        }

    }

    /**
     * 自动添加帆软maven仓库
     * @param project
     */
    private void addFanruanRepositories(Project project) {
        def repositories = project.repositories
        repositories.maven {
            url FANRUAN_MAVEN
        }
    }

}

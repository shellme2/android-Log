package com.eebbk.bfc.bfclog.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

/**
 * Created by Simon on 2016/9/6.
 */
class BfcLogPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        def isApp = project.plugins.withType( AppPlugin )
        def isLib = project.plugins.withType( LibraryPlugin )

        if (!isApp && !isLib){
            throw new IllegalStateException("'android' or 'android-library' plugin required. ")
        }

        final def log = project.logger
        final def variants

        if (isApp){
            variants = project.android.applicationVariants
        }else {
            variants = project.android.libraryVariants
        }

        project.dependencies {
            compile 'com.eebbk.bfc:bfc-log-aop:2.0.1'
        }

        project.extensions.create("BfcLog", BfcLogExtension)

        variants.all { variant ->
            println ">>>>>>>>>>>>>> 进入BfcLog插件 <<<<<<<<<<<<<<<<<<<<"
            println "enable=" + project.BfcLog.enable +"   variant.buildType.isDebuggable()=" + variant.buildType.isDebuggable()

           /* if (!variant.buildType.isDebuggable()) {
                log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")
                println ">>>> 不是debug版本, 跳过编译BfcLog插件"
                return;
            } else if (!project.BfcLog.enable) {
                log.debug("BfcLogTime is not disabled.")
                return;
            }*/

            JavaCompile javaCompile = variant.javaCompile
            javaCompile.doLast {
                String[] args = [
                        "-showWeaveInfo",
                        "-1.5",
                        "-inpath", javaCompile.destinationDir.toString(),
                        "-aspectpath", javaCompile.classpath.asPath,
                        "-d", javaCompile.destinationDir.toString(),
                        "-classpath", javaCompile.classpath.asPath,
                        "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)
                ]
                log.debug "ajc args: " + Arrays.toString(args)

                MessageHandler handler = new MessageHandler(true);
                new Main().run(args, handler);
                for (IMessage message : handler.getMessages(null, true)) {
                    switch (message.getKind()) {
                        case IMessage.ABORT:
                        case IMessage.ERROR:
                        case IMessage.FAIL:
                            log.error message.message, message.thrown
                            break;
                        case IMessage.WARNING:
                            log.warn message.message, message.thrown
                            break;
                        case IMessage.INFO:
                            log.info message.message, message.thrown
                            break;
                        case IMessage.DEBUG:
                            log.debug message.message, message.thrown
                            break;
                    }
                }
            }
        }

    }
}

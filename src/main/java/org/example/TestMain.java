package org.example;


import io.reactivex.Completable;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.stream.Collectors;

@Slf4j
public class TestMain extends AbstractVerticle {
    @Override
    public Completable rxStart(){
        log.info("Initializing JavaScriptEvaluator");
        ScriptEngineManager sem = new ScriptEngineManager();
        try {
            log.info(
                    "available " +
                            sem.getEngineFactories().stream()
                                    .map(f -> f.getLanguageName() + " " + f.getEngineName() + " " + f.getEngineVersion())
                                    .collect(Collectors.toList()));
        } catch (Exception e){
            log.info("Error on reading available languages", e);
        }

        ScriptEngine jsEngine = sem.getEngineByName("js");

        // reading math.js lib from file and eval in jsEngine
        return vertx.fileSystem().rxReadFile("math.js")
                .doOnSuccess(buffer -> jsEngine.eval(buffer.toString()))
                .doOnError(onError -> log.error("error occurred at JavaScript Processor", onError))
                .ignoreElement()
                .doOnComplete(() -> log.info("JavaScriptEvaluator initialized successfully !"));
    }

    public static void main(String[] args) {
        VertxOptions vertxOptions = new VertxOptions();

        DeploymentOptions workerOpts = new DeploymentOptions()
                .setWorker(true)
                .setInstances(1)
                .setWorkerPoolSize(5);

        Vertx.vertx(vertxOptions)
                .rxDeployVerticle((new TestMain()), workerOpts)
                .doOnSuccess(deployId -> log.info("Service Deployment id is {} ", deployId))
                .ignoreElement()
                .subscribe(
                        () ->{},
                        onError -> log.error("Error on deploying service ", onError)
                );
    }
}

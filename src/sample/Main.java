package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    /*


set PATH=%JAVA_HOME%/jre/bin;%PATH%
set SAND_BOX_HOME=d:/sandbox
set TOKEN="kjhdf783h5dfg"
java -Xms128M -Xmx128M -Xnoclassgc -ea -Xbootclasspath/a:"%JAVA_HOME%/lib/tools.jar" -jar %SAND_BOX_HOME%/lib/sandbox-core.jar 1 "%SAND_BOX_HOME%/lib/sandbox-agent.jar" home="%SAND_BOX_HOME%";token=%TOKEN%;service.ip=0.0.0.0;server.port=8888;namespace=default

http://127.0.0.1:8888/sandbox/default/module/http/sandbox-module-mgr/list?1=1&


     */

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("VJvmSandBox");
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

package sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    @FXML
    TextField sandBoxUrl;

    @FXML
    TextField namespaces;

    @FXML
    TextField inputCommand;

    @FXML
    TextArea console;

    @FXML
    ListView<String> systemCtrl;

    PrintStream consoleStream = null;

    List<String> modules = new ArrayList<String>();

    public void loadJvmSandBox(final Event event) {
        Button button = ((Button) event.getSource());
        if (button.getText().equals("连接")) {
            ((Button) event.getSource()).setText("断开");
            sandBoxUrl.setDisable(true);
            namespaces.setDisable(true);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    if (consoleStream == null) {
                        consoleStream = new PrintStream(new OutputStream() {
                            @Override
                            public void write(int b) {
                                final String text = String.valueOf((char) b);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        console.appendText(text);
                                    }
                                });
                            }

                            @Override
                            public void write(byte[] b, int off, int len) {
                                final String text = new String(b, off, len);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        console.appendText(text);
                                    }
                                });
                            }
                        }, true);
                        System.setOut(consoleStream);
                        System.setErr(consoleStream);
                    }

                    System.out.println("event = " + event);
                    System.out.println("sandBoxUrl = " + sandBoxUrl.getText());
                    System.out.println("namespaces = " + namespaces.getText());
                    try {
                        String modulesString = getMessage("sandbox-module-mgr", "list");
                        for (String s : modulesString.split("\n")) {
                            if (s.startsWith("total")) continue;
                            modules.add(s.split(" ")[0]);
                        }

                        ObservableList<String> items = systemCtrl.getItems();

                        items.add("JVM SandBox version");
                        items.add("list module");
                        items.add("flush module");
                        items.add("flush module force");
                        items.add("reset module");
                        for (String module : modules) {
                            items.add("unload " + module);
                            items.add("active " + module);
                            items.add("frozen " + module);
                            items.add("detail " + module);
                        }

                        items.add("shutdown sandbox");

                    } catch (Exception e) {
                        e.printStackTrace();
                        sandBoxUrl.setDisable(false);
                        namespaces.setDisable(false);
                    }
                }
            });
        } else {
            modules.clear();
            systemCtrl.getItems().clear();
            sandBoxUrl.setDisable(false);
            namespaces.setDisable(false);
            ((Button) event.getSource()).setText("连接");
        }
    }

    String lastSystemCommand;

    public void execSystemCommand(MouseEvent event) {
        if (event.getClickCount() != 2) {
            return;
        }
        final String selected = systemCtrl.getSelectionModel().getSelectedItem();

        if (selected.equals(lastSystemCommand)) {
            return;
        } else {
            lastSystemCommand = selected;
        }

        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (selected.equals("JVM SandBox version")) {
                    System.out.println("> JVM SandBox version");
                    getMessage("sandbox-info", "version", System.out);
                } else if (selected.equals("list module")) {
                    System.out.println("> list module");
                    System.out.println(getMessage("sandbox-module-mgr", "list"));
                } else if (selected.equals("flush module")) {
                    System.out.println("> flush module");
                    System.out.println(getMessage("sandbox-module-mgr", "flush", "&force=false"));
                } else if (selected.equals("flush module force")) {
                    System.out.println("> flush module force");
                    System.out.println(getMessage("sandbox-module-mgr", "flush", "&force=true"));
                } else if (selected.equals("reset module")) {
                    System.out.println("> reset module");
                    System.out.println(getMessage("sandbox-module-mgr", "reset"));
                } else if (selected.startsWith("unload ")) {
                    System.out.println("> unload module " + selected.split(" ")[1]);
                    System.out.println(getMessage("sandbox-module-mgr", "unload", "&action=unload&ids=" + selected.split(" ")[1]));
                } else if (selected.startsWith("active ")) {
                    System.out.println("> active module " + selected.split(" ")[1]);
                    System.out.println(getMessage("sandbox-module-mgr", "active", "&ids=" + selected.split(" ")[1]));
                } else if (selected.startsWith("frozen")) {
                    System.out.println("> frozen module " + selected.split(" ")[1]);
                    System.out.println(getMessage("sandbox-module-mgr", "frozen", "&ids=" + selected.split(" ")[1]));
                } else if (selected.startsWith("detail ")) {
                    System.out.println("> module detail " + selected.split(" ")[1]);
                    System.out.println(getMessage("sandbox-module-mgr", "detail", "&id=" + selected.split(" ")[1]));
                } else if (selected.equals("shutdown sandbox")) {
                    System.out.println("> shutdown sandbox");
                    System.out.println(getMessage("sandbox-control", "shutdown"));
                }
            }
        });

    }

    public void onInputCommandKeyPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            final String commandString = inputCommand.getText().trim();
            System.out.println("> " + commandString);
            inputCommand.clear();
            Platform.runLater(
                    new Runnable() {

                        @Override
                        public void run() {

                            String[] commandInfo = commandString.split(" ");
                            List<String> info = new ArrayList<String>();
                            for (String s : commandInfo) {
                                if (s.trim().isEmpty()) continue;
                                info.add(s.trim());
                            }

                            if (info.size() >= 2) {
                                String module = info.get(0);
                                String command = info.get(1);
                                String params = "";
                                for (int i = 2; i < info.size(); i++) {
                                    params += "&" + info.get(i);
                                }
                                getMessage(module, command, params, System.out);

                            } else {
                                System.out.println("command format: <MODULE> <COMMAND> [?<PARAM1=VALUE1> [<PARAM2=VALUE2> ...]]");
                            }
                        }
                    }
            );
        }
    }

    private String getMessage(String module, String command) {
        return getMessage(module, command, "");
    }

    private String getMessage(String module, String command, String param) {
        return httpGet(String.format("%s/sandbox/%s/module/http/%s/%s?1=1&%s", sandBoxUrl.getText(), namespaces.getText(), module, command, param));
    }

    private void getMessage(String module, String command, OutputStream outputStream) {
        getMessage(module, command, "", outputStream);
    }

    private void getMessage(String module, String command, String param, OutputStream outputStream) {
        httpGet(String.format("%s/sandbox/%s/module/http/%s/%s?1=1&%s", sandBoxUrl.getText(), namespaces.getText(), module, command, param), outputStream);
    }

    public static void httpGet(String url, OutputStream outputStream) {
        //get请求返回结果
        String strResult = "";
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            //发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                response.getEntity().writeTo(outputStream);

                url = URLDecoder.decode(url, "UTF-8");
            } else {
                throw new RuntimeException("request submission failed:" + url);
            }
        } catch (IOException e) {
            throw new RuntimeException("request submission failed:" + url, e);
        }
    }

    public static String httpGet(String url) {
        //get请求返回结果
        String strResult = "";
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            //发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                strResult = EntityUtils.toString(response.getEntity());
                url = URLDecoder.decode(url, "UTF-8");
            } else {
                throw new RuntimeException("request submission failed:" + url);
            }
        } catch (IOException e) {
            throw new RuntimeException("request submission failed:" + url, e);
        }
        return strResult;
    }
}

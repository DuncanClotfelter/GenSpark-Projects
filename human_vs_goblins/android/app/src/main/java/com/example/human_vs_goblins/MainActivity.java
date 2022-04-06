package com.example.human_vs_goblins;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import lombok.Getter;

import java.util.ArrayList;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.example.humans_vs_goblins";
    @Getter private static MethodChannel platform;
    static Grid game;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        GameLauncher.startGame(false);
        platform = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);
        platform.setMethodCallHandler(
                (call, result) -> {
                    try {
                        switch (call.method) {
                            case "setPlayerDir":
                                ArrayList<Integer> i = call.arguments();
                                System.out.println("Setting player dir!");
                                game.getPlayer().getMoveInput().put(new Point(i.get(0), i.get(1)));
                                break;
                            case "getEntities":
                                result.success(game.entToIntArr());
                                break;
                            case "getTiles":
                                result.success(game.mapToIntArr());
                                game.updateEntities();
                                break;
                            default:
                                result.notImplemented();
                                break;
                        }
                    } catch(Exception e) {
                        System.out.println("Ignoring user input!");
                        e.printStackTrace();
                    }
                }
        );
    }
}
package com.example.a23110035_23110060.controller;

import android.content.Context;

import com.example.a23110035_23110060.data.local.GoalEntity;
import com.example.a23110035_23110060.data.local.UserEntity;
import com.example.a23110035_23110060.data.repository.GoalRepository;
import com.example.a23110035_23110060.data.repository.UserRepository;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;

public class SettingsController {
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final AuthController authController;

    public SettingsController(Context context) {
        goalRepository = new GoalRepository(context);
        userRepository = new UserRepository(context);
        authController = new AuthController(context);
    }

    public void loadGoal(RepositoryCallback<GoalEntity> callback) {
        goalRepository.getGoal(callback);
    }

    public void saveGoal(GoalEntity goal, RepositoryCallback<GoalEntity> callback) {
        goalRepository.saveGoal(goal, callback);
    }

    public void loadUser(RepositoryCallback<UserEntity> callback) {
        userRepository.getCurrentUserLocal(callback);
    }

    public void saveUser(UserEntity user, RepositoryCallback<Void> callback) {
        userRepository.saveUserLocal(user, callback);
    }

    public void logout() {
        authController.logout();
    }
}

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter/material.dart';

import '../../domain/usecases/manage_queue.dart';
import '../../domain/usecases/manage_library.dart';
import '../../data/repositories/settings_repository.dart';
import '../../core/constants.dart';
import '../../domain/entities/settings_state.dart';

class SettingsController extends StateNotifier<SettingsState> {
  final SettingsRepository _repository;

  SettingsController(this._repository) : super(const SettingsState());

  Future<void> load() async {
    state = state.copyWith(isLoading: true);
    final result = await _repository.loadSettings();
    result.fold(
      (failure) => state = state.copyWith(isLoading: false, error: failure.userMessage),
      (settings) => state = settings.copyWith(isLoading: false),
    );
  }

  Future<void> setThemeMode(ThemeMode mode) async {
    state = state.copyWith(themeMode: mode);
    await _repository.saveSettings(state);
  }

  Future<void> setAudioQuality(AudioQuality quality) async {
    state = state.copyWith(audioQuality: quality);
    await _repository.saveSettings(state);
  }

  Future<void> setThumbnailQuality(ThumbnailQuality quality) async {
    state = state.copyWith(thumbnailQuality: quality);
    await _repository.saveSettings(state);
  }

  Future<void> setCountry(String country) async {
    state = state.copyWith(country: country);
    await _repository.saveSettings(state);
  }

  Future<void> setAutoPlay(bool value) async {
    state = state.copyWith(autoPlay: value);
    await _repository.saveSettings(state);
  }

  Future<void> setClearCacheOnExit(bool value) async {
    state = state.copyWith(clearCacheOnExit: value);
    await _repository.saveSettings(state);
  }

  Future<void> clearCache() async {
    // TODO: implement cache clearing
  }

  Future<void> resetToDefaults() async {
    await _repository.resetToDefaults();
    state = const SettingsState();
  }
}
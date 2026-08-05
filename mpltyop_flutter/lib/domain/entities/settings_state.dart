import 'package:freezed_annotation/freezed_annotation.dart';
import '../../core/constants.dart';

part 'settings_state.freezed.dart';

@freezed
class SettingsState with _$SettingsState {
  const factory SettingsState({
    @Default(ThemeMode.system) ThemeMode themeMode,
    @Default(AppConstants.defaultAudioQuality) AudioQuality audioQuality,
    @Default(AppConstants.defaultThumbQuality) ThumbnailQuality thumbnailQuality,
    @Default(AppConstants.defaultCountry) String country,
    @Default(true) bool autoPlay,
    @Default(false) bool clearCacheOnExit,
    @Default(false) bool isLoading,
    String? error,
  }) = _SettingsState;
}
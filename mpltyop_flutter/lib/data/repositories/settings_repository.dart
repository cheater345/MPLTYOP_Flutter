import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants.dart';
import '../../core/errors.dart';
import '../entities/settings_state.dart';

abstract class SettingsRepository {
  Future<Either<Failure, SettingsState>> loadSettings();
  Future<Either<Failure, void>> saveSettings(SettingsState settings);
  Future<Either<Failure, void>> resetToDefaults();
}

class SettingsRepositoryImpl implements SettingsRepository {
  final Future<SharedPreferences> _prefsFuture;

  SettingsRepositoryImpl(this._prefsFuture);

  @override
  Future<Either<Failure, SettingsState>> loadSettings() async {
    try {
      final prefs = await _prefsFuture;
      return Right(SettingsState(
        themeMode: ThemeMode.values[prefs.getInt(AppConstants.keyThemeMode) ?? 0],
        audioQuality: AudioQuality.values[prefs.getInt(AppConstants.keyAudioQuality) ?? AppConstants.defaultAudioQuality.index],
        thumbnailQuality: ThumbnailQuality.values[prefs.getInt(AppConstants.keyThumbQuality) ?? AppConstants.defaultThumbQuality.index],
        country: prefs.getString(AppConstants.keyCountry) ?? AppConstants.defaultCountry,
        autoPlay: prefs.getBool(AppConstants.keyAutoPlay) ?? true,
        clearCacheOnExit: prefs.getBool(AppConstants.keyClearCacheOnExit) ?? false,
      ));
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> saveSettings(SettingsState settings) async {
    try {
      final prefs = await _prefsFuture;
      await prefs.setInt(AppConstants.keyThemeMode, settings.themeMode.index);
      await prefs.setInt(AppConstants.keyAudioQuality, settings.audioQuality.index);
      await prefs.setInt(AppConstants.keyThumbQuality, settings.thumbnailQuality.index);
      await prefs.setString(AppConstants.keyCountry, settings.country);
      await prefs.setBool(AppConstants.keyAutoPlay, settings.autoPlay);
      await prefs.setBool(AppConstants.keyClearCacheOnExit, settings.clearCacheOnExit);
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> resetToDefaults() async {
    try {
      final prefs = await _prefsFuture;
      await prefs.clear();
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }
}
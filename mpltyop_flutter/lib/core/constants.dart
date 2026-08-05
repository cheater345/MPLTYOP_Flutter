enum AudioQuality { low, medium, high, veryHigh }
enum ThumbnailQuality { low, medium, high, veryHigh }

extension AudioQualityExt on AudioQuality {
  String get name => switch (this) {
    AudioQuality.low => 'LOW',
    AudioQuality.medium => 'MED',
    AudioQuality.high => 'HIGH',
    AudioQuality.veryHigh => 'VERY_HIGH',
  };
}

extension ThumbnailQualityExt on ThumbnailQuality {
  String get name => switch (this) {
    ThumbnailQuality.low => 'LOW',
    ThumbnailQuality.medium => 'MED',
    ThumbnailQuality.high => 'HIGH',
    ThumbnailQuality.veryHigh => 'VERY_HIGH',
  };
}

class AppConstants {
  static const String appName = 'MPLTYOP';
  static const String packageName = 'com.cheater345.mpltyop';
  static const String defaultCountry = 'US';
  static const AudioQuality defaultAudioQuality = AudioQuality.veryHigh;
  static const ThumbnailQuality defaultThumbQuality = ThumbnailQuality.veryHigh;
  static const int defaultSearchLimit = 20;
  static const int maxQueueSize = 500;
  static const Duration audioUrlExpiry = Duration(hours: 6);
  
  static const String boxTracks = 'tracks';
  static const String boxPlaylists = 'playlists';
  static const String boxLiked = 'liked';
  static const String boxQueue = 'queue';
  static const String boxHistory = 'history';
  static const String boxSettings = 'settings';
  
  static const String keyThemeMode = 'theme_mode';
  static const String keyAudioQuality = 'audio_quality';
  static const String keyThumbQuality = 'thumb_quality';
  static const String keyCountry = 'country';
  static const String keyAutoPlay = 'auto_play';
  static const String keyDownloadQuality = 'download_quality';
  static const String keyClearCacheOnExit = 'clear_cache_on_exit';
}
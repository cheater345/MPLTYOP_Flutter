import 'package:yt_flutter_musicapi/yt_flutter_musicapi.dart';

import '../../core/constants.dart' as app hide AudioQuality, ThumbnailQuality;
import '../../domain/entities/track.dart';

class YtMusicApi {
  final YtFlutterMusicapi _plugin = YtFlutterMusicapi();
  bool _initialized = false;

  Future<void> initialize() async {
    if (_initialized) return;
    await _plugin.initialize(
      country: app.AppConstants.defaultCountry,
      proxy: null,
    );
    _initialized = true;
  }

  Stream<Track> streamSearch({
    required String query,
    int limit = app.AppConstants.defaultSearchLimit,
    AudioQuality quality = AudioQuality.veryHigh,
    ThumbnailQuality thumbQuality = ThumbnailQuality.veryHigh,
  }) async* {
    await initialize();
    await for (final result in YtFlutterMusicapi().streamSearchResults(
      query: query,
      limit: limit,
      audioQuality: quality,
      thumbQuality: thumbQuality,
      includeAudioUrl: true,
      includeAlbumArt: true,
    )) {
      yield Track.fromYtResult(result.toMap());
    }
  }

  Future<List<Track>> search({
    required String query,
    int limit = app.AppConstants.defaultSearchLimit,
    AudioQuality quality = AudioQuality.veryHigh,
    ThumbnailQuality thumbQuality = ThumbnailQuality.veryHigh,
  }) async {
    await initialize();
    final result = await YtFlutterMusicapi().searchMusic(
      query: query,
      limit: limit,
      audioQuality: quality,
      thumbQuality: thumbQuality,
      includeAudioUrl: true,
      includeAlbumArt: true,
    );
    final tracks = (result.data ?? [])
        .map((s) => Track.fromSearchResult(s.toMap()))
        .toList();
    return tracks;
  }

  Future<List<Track>> getRelated({
    required String videoId,
    int limit = 20,
  }) async {
    await initialize();
    // Note: yt_flutter_musicapi may not have direct related endpoint
    // Use search with video title as fallback
    return [];
  }

  Future<String?> getLyrics(String videoId) async {
    await initialize();
    // Note: yt_flutter_musicapi may not have lyrics endpoint
    return null;
  }

  Future<Track?> getTrackDetails(String videoId) async {
    await initialize();
    final tracks = await search(query: videoId, limit: 1);
    return tracks.isNotEmpty ? tracks.first : null;
  }
}
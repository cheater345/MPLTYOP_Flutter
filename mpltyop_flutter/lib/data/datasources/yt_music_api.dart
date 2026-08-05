import 'package:yt_flutter_musicapi/yt_flutter_musicapi.dart';
import '../../core/constants.dart';
import '../entities/track.dart';

class YtMusicApi {
  final YtFlutterMusicapi _plugin = YtFlutterMusicapi();
  bool _initialized = false;

  Future<void> initialize() async {
    if (_initialized) return;
    await _plugin.initialize(
      country: AppConstants.defaultCountry,
      proxy: null,
    );
    _initialized = true;
  }

  Stream<Track> streamSearch({
    required String query,
    int limit = AppConstants.defaultSearchLimit,
    AudioQuality quality = AppConstants.defaultAudioQuality,
    ThumbnailQuality thumbQuality = AppConstants.defaultThumbQuality,
  }) async* {
    await initialize();
    await for (final result in YtFlutterMusicapi().streamSearchResults(
      query: query,
      limit: limit,
      audioQuality: quality.name,
      thumbQuality: thumbQuality.name,
      includeAudioUrl: true,
      includeAlbumArt: true,
    )) {
      yield Track.fromYtResult(result as Map<String, dynamic>);
    }
  }

  Future<List<Track>> search({
    required String query,
    int limit = AppConstants.defaultSearchLimit,
    AudioQuality quality = AppConstants.defaultAudioQuality,
    ThumbnailQuality thumbQuality = AppConstants.defaultThumbQuality,
  }) async {
    await initialize();
    final result = await YtFlutterMusicapi().searchMusic(
      query: query,
      limit: limit,
      audioQuality: quality.name,
      thumbQuality: thumbQuality.name,
      includeAudioUrl: true,
      includeAlbumArt: true,
    );
    final tracks = (result['tracks'] as List? ?? [])
        .map((t) => Track.fromYtResult(t as Map<String, dynamic>))
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
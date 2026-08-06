import 'dart:async';

import 'package:yt_flutter_musicapi/yt_flutter_musicapi.dart';

import '../../core/constants.dart' as app hide AudioQuality, ThumbnailQuality;
import '../../domain/entities/track.dart';

class YtMusicApi {
  final YtFlutterMusicapi _plugin = YtFlutterMusicapi();
  Future<void>? _initFuture;

  Future<void> initialize() {
    return _initFuture ??= _doInitialize();
  }

  Future<void> _doInitialize() async {
    await _plugin.initialize(
      country: app.AppConstants.defaultCountry,
      proxy: null,
    );

    // The plugin returns immediately and finishes booting Python in the
    // background. Wait (with retries) until ytmusicapi and yt-dlp are ready,
    // otherwise the first searches fail before init completes.
    const maxAttempts = 12;
    for (var attempt = 1; attempt <= maxAttempts; attempt++) {
      final status = await _plugin.checkStatus();
      if (status.success && status.data?.isFullyOperational == true) {
        return;
      }
      await Future<void>.delayed(const Duration(seconds: 3));
    }

    final last = await _plugin.checkStatus();
    throw Exception(
      'Python not ready: ${last.message} '
      '(${last.data?.statusSummary ?? 'unknown'})',
    );
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
    if (!result.success) {
      throw Exception(result.error ?? 'Search failed');
    }
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

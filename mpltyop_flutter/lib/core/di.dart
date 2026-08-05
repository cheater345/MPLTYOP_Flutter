import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../data/datasources/yt_music_api.dart';
import '../data/repositories/music_repository.dart';
import '../data/repositories/storage_repository.dart';
import '../data/repositories/settings_repository.dart';
import '../domain/usecases/search_tracks.dart';
import '../domain/usecases/get_related.dart';
import '../domain/usecases/get_lyrics.dart';
import '../domain/usecases/manage_queue.dart';
import '../domain/usecases/manage_library.dart';
import '../services/playback_service.dart';
import 'constants.dart';

final hiveProvider = Provider<HiveInterface>((ref) => Hive);

final sharedPreferencesProvider = FutureProvider<SharedPreferences>((ref) async {
  return await SharedPreferences.getInstance();
});

final ytMusicApiProvider = Provider<YtMusicApi>((ref) => YtMusicApi());

final musicRepositoryProvider = Provider<MusicRepository>((ref) {
  return MusicRepositoryImpl(
    ref.watch(ytMusicApiProvider),
    ref.watch(storageRepositoryProvider),
  );
});

final storageRepositoryProvider = Provider<StorageRepository>((ref) {
  return StorageRepositoryImpl(
    ref.watch(hiveProvider),
    ref.watch(sharedPreferencesProvider.future),
  );
});

final settingsRepositoryProvider = Provider<SettingsRepository>((ref) {
  return SettingsRepositoryImpl(ref.watch(sharedPreferencesProvider.future));
});

final searchTracksProvider = Provider<SearchTracks>((ref) {
  return SearchTracks(ref.watch(musicRepositoryProvider));
});

final getRelatedProvider = Provider<GetRelated>((ref) {
  return GetRelated(ref.watch(musicRepositoryProvider));
});

final getLyricsProvider = Provider<GetLyrics>((ref) {
  return GetLyrics(ref.watch(musicRepositoryProvider));
});

final manageQueueProvider = Provider<ManageQueue>((ref) {
  return ManageQueue(
    ref.watch(storageRepositoryProvider),
    ref.watch(playbackServiceProvider),
  );
});

final manageLibraryProvider = Provider<ManageLibrary>((ref) {
  return ManageLibrary(ref.watch(storageRepositoryProvider));
});

final playbackServiceProvider = Provider<PlaybackService>((ref) {
  return PlaybackService(
    ref.watch(musicRepositoryProvider),
    ref.watch(storageRepositoryProvider),
  );
});

final settingsControllerProvider = StateNotifierProvider<SettingsController, SettingsState>((ref) {
  return SettingsController(ref.watch(settingsRepositoryProvider));
});

final searchControllerProvider = StateNotifierProvider<SearchController, AsyncValue<List<Track>>>((ref) {
  return SearchController(ref.watch(musicRepositoryProvider));
});

final queueControllerProvider = StateNotifierProvider<QueueController, QueueState>((ref) {
  return QueueController(
    ref.watch(storageRepositoryProvider),
    ref.watch(playbackServiceProvider),
  );
});

final libraryControllerProvider = StateNotifierProvider<LibraryController, LibraryState>((ref) {
  return LibraryController(ref.watch(storageRepositoryProvider));
});

final playbackStateProvider = StreamProvider<PlaybackState>((ref) {
  return ref.watch(playbackServiceProvider).playbackStateStream;
});

final currentTrackProvider = Provider<Track?>((ref) {
  final state = ref.watch(playbackStateProvider);
  return state.maybeWhen(
    data: (s) => s.currentTrack,
    orElse: () => null,
  );
});
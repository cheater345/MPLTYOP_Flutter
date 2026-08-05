import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/usecases/manage_library.dart';
import '../../domain/entities/library_state.dart';
import '../../domain/entities/playlist.dart';

class LibraryController extends StateNotifier<LibraryState> {
  final ManageLibrary _manageLibrary;

  LibraryController(this._manageLibrary) : super(const LibraryState());

  Future<void> load() async {
    state = state.copyWith(isLoading: true);
    try {
      final playlistsResult = await _manageLibrary.getPlaylists();
      final likedResult = await _manageLibrary.getLikedTrackIds();
      final historyResult = await _manageLibrary.getHistoryTrackIds();

      if (playlistsResult.isRight() && likedResult.isRight() && historyResult.isRight()) {
        state = state.copyWith(
          playlists: playlistsResult.valueOrNull ?? [],
          likedTrackIds: likedResult.valueOrNull ?? [],
          historyTrackIds: historyResult.valueOrNull ?? [],
          isLoading: false,
        );
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> createPlaylist(String name) async {
    final playlist = Playlist(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      name: name,
      createdAt: DateTime.now(),
    );
    await _manageLibrary.createPlaylist(playlist);
    state = state.copyWith(playlists: [playlist, ...state.playlists]);
  }

  Future<void> deletePlaylist(String id) async {
    await _manageLibrary.deletePlaylist(id);
    state = state.copyWith(playlists: state.playlists.where((p) => p.id != id).toList());
  }

  Future<void> addTrackToPlaylist(String playlistId, String trackId) async {
    await _manageLibrary.addTrackToPlaylist(playlistId, trackId);
    final idx = state.playlists.indexWhere((p) => p.id == playlistId);
    if (idx >= 0) {
      final updated = state.playlists[idx].copyWithTracks([...state.playlists[idx].trackIds, trackId]);
      state = state.copyWith(playlists: [
        ...state.playlists.take(idx),
        updated,
        ...state.playlists.skip(idx + 1),
      ]);
    }
  }

  Future<void> removeTrackFromPlaylist(String playlistId, String trackId) async {
    await _manageLibrary.removeTrackFromPlaylist(playlistId, trackId);
    final idx = state.playlists.indexWhere((p) => p.id == playlistId);
    if (idx >= 0) {
      final updated = state.playlists[idx].copyWithTracks(
        state.playlists[idx].trackIds.where((id) => id != trackId).toList(),
      );
      state = state.copyWith(playlists: [
        ...state.playlists.take(idx),
        updated,
        ...state.playlists.skip(idx + 1),
      ]);
    }
  }

  Future<void> reorderPlaylistTracks(String playlistId, int oldIndex, int newIndex) async {
    await _manageLibrary.reorderPlaylistTracks(playlistId, oldIndex, newIndex);
    final idx = state.playlists.indexWhere((p) => p.id == playlistId);
    if (idx >= 0) {
      final trackIds = [...state.playlists[idx].trackIds];
      final item = trackIds.removeAt(oldIndex);
      trackIds.insert(newIndex, item);
      final updated = state.playlists[idx].copyWithTracks(trackIds);
      state = state.copyWith(playlists: [
        ...state.playlists.take(idx),
        updated,
        ...state.playlists.skip(idx + 1),
      ]);
    }
  }

  Future<void> toggleLiked(String trackId) async {
    final isLiked = state.likedTrackIds.contains(trackId);
    await _manageLibrary.toggleLiked(trackId, isLiked);
    state = state.copyWith(
      likedTrackIds: isLiked 
        ? state.likedTrackIds.where((id) => id != trackId).toList()
        : [trackId, ...state.likedTrackIds],
    );
  }

  Future<void> addToHistory(String trackId) async {
    await _manageLibrary.addToHistory(trackId);
    final newHistory = [trackId, ...state.historyTrackIds.where((id) => id != trackId)].take(100).toList();
    state = state.copyWith(historyTrackIds: newHistory);
  }
}
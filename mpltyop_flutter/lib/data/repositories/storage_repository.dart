import 'package:hive/hive.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants.dart';
import '../../core/errors.dart';
import '../../domain/entities/track.dart';
import '../../domain/entities/playlist.dart';

abstract class StorageRepository {
  // Tracks
  Future<Either<Failure, void>> cacheTrack(Track track);
  Future<Either<Failure, Track?>> getCachedTrack(String id);
  Future<Either<Failure, void>> removeCachedTrack(String id);
  Future<Either<Failure, void>> clearTrackCache();

  // Playlists
  Future<Either<Failure, List<Playlist>>> getPlaylists();
  Future<Either<Failure, void>> savePlaylist(Playlist playlist);
  Future<Either<Failure, void>> deletePlaylist(String id);
  Future<Either<Failure, void>> addTrackToPlaylist(String playlistId, String trackId);
  Future<Either<Failure, void>> removeTrackFromPlaylist(String playlistId, String trackId);
  Future<Either<Failure, void>> reorderPlaylistTracks(String playlistId, int oldIndex, int newIndex);

  // Liked tracks
  Future<Either<Failure, List<String>>> getLikedTrackIds();
  Future<Either<Failure, void>> addLikedTrack(String trackId);
  Future<Either<Failure, void>> removeLikedTrack(String trackId);

  // Queue
  Future<Either<Failure, List<String>>> getQueueTrackIds();
  Future<Either<Failure, void>> saveQueue(List<String> trackIds, int currentIndex);
  Future<Either<Failure, void>> clearQueue();

  // History
  Future<Either<Failure, List<String>>> getHistoryTrackIds();
  Future<Either<Failure, void>> addToHistory(String trackId);
  Future<Either<Failure, void>> clearHistory();
}

class StorageRepositoryImpl implements StorageRepository {
  final HiveInterface _hive;
  final Future<SharedPreferences> _prefsFuture;

  StorageRepositoryImpl(this._hive, this._prefsFuture);

  Box<Track> get _tracksBox => _hive.box<Track>(AppConstants.boxTracks);
  Box<Playlist> get _playlistsBox => _hive.box<Playlist>(AppConstants.boxPlaylists);
  Box<String> get _likedBox => _hive.box<String>(AppConstants.boxLiked);
  Box<String> get _queueBox => _hive.box<String>(AppConstants.boxQueue);
  Box<String> get _historyBox => _hive.box<String>(AppConstants.boxHistory);
  Future<SharedPreferences> get _prefs => _prefsFuture;

  // Tracks
  @override
  Future<Either<Failure, void>> cacheTrack(Track track) async {
    try {
      await _tracksBox.put(track.id, track);
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, Track?>> getCachedTrack(String id) async {
    try {
      final track = _tracksBox.get(id);
      return Right(track);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> removeCachedTrack(String id) async {
    try {
      await _tracksBox.delete(id);
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> clearTrackCache() async {
    try {
      await _tracksBox.clear();
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  // Playlists
  @override
  Future<Either<Failure, List<Playlist>>> getPlaylists() async {
    try {
      final playlists = _playlistsBox.values.toList();
      playlists.sort((a, b) => b.createdAt.compareTo(a.createdAt));
      return Right(playlists);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> savePlaylist(Playlist playlist) async {
    try {
      await _playlistsBox.put(playlist.id, playlist);
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> deletePlaylist(String id) async {
    try {
      await _playlistsBox.delete(id);
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> addTrackToPlaylist(String playlistId, String trackId) async {
    try {
      final playlist = _playlistsBox.get(playlistId);
      if (playlist != null && !playlist.trackIds.contains(trackId)) {
        final updated = playlist.copyWithTracks([...playlist.trackIds, trackId]);
        await _playlistsBox.put(playlistId, updated);
      }
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> removeTrackFromPlaylist(String playlistId, String trackId) async {
    try {
      final playlist = _playlistsBox.get(playlistId);
      if (playlist != null) {
        final updated = playlist.copyWithTracks(
          playlist.trackIds.where((id) => id != trackId).toList(),
        );
        await _playlistsBox.put(playlistId, updated);
      }
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> reorderPlaylistTracks(String playlistId, int oldIndex, int newIndex) async {
    try {
      final playlist = _playlistsBox.get(playlistId);
      if (playlist != null) {
        final trackIds = [...playlist.trackIds];
        final item = trackIds.removeAt(oldIndex);
        trackIds.insert(newIndex, item);
        final updated = playlist.copyWithTracks(trackIds);
        await _playlistsBox.put(playlistId, updated);
      }
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  // Liked tracks
  @override
  Future<Either<Failure, List<String>>> getLikedTrackIds() async {
    try {
      return Right(_likedBox.values.toList());
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> addLikedTrack(String trackId) async {
    try {
      if (!_likedBox.values.contains(trackId)) {
        await _likedBox.add(trackId);
      }
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> removeLikedTrack(String trackId) async {
    try {
      final key = _likedBox.keys.firstWhere((k) => _likedBox.get(k) == trackId, orElse: () => null);
      if (key != null) await _likedBox.delete(key);
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  // Queue
  @override
  Future<Either<Failure, List<String>>> getQueueTrackIds() async {
    try {
      return Right(_queueBox.values.toList());
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> saveQueue(List<String> trackIds, int currentIndex) async {
    try {
      await _queueBox.clear();
      for (final id in trackIds) {
        await _queueBox.add(id);
      }
      final prefs = await _prefs;
      await prefs.setInt('queue_current_index', currentIndex);
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> clearQueue() async {
    try {
      await _queueBox.clear();
      final prefs = await _prefs;
      await prefs.remove('queue_current_index');
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  // History
  @override
  Future<Either<Failure, List<String>>> getHistoryTrackIds() async {
    try {
      return Right(_historyBox.values.toList());
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> addToHistory(String trackId) async {
    try {
      final history = _historyBox.values.toList();
      history.remove(trackId);
      history.insert(0, trackId);
      if (history.length > 100) history.removeLast();
      await _historyBox.clear();
      for (final id in history) {
        await _historyBox.add(id);
      }
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> clearHistory() async {
    try {
      await _historyBox.clear();
      return const Right(null);
    } catch (e) {
      return Left(Failure.storageFailed(e.toString()));
    }
  }
}
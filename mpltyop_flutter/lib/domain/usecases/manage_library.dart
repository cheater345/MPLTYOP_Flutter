import '../../data/repositories/storage_repository.dart';
import '../entities/playlist.dart';
import '../../core/errors.dart';

class ManageLibrary {
  final StorageRepository _storage;

  ManageLibrary(this._storage);

  // Playlists
  Future<Either<Failure, List<Playlist>>> getPlaylists() => _storage.getPlaylists();
  Future<Either<Failure, void>> createPlaylist(Playlist playlist) => _storage.savePlaylist(playlist);
  Future<Either<Failure, void>> deletePlaylist(String id) => _storage.deletePlaylist(id);
  Future<Either<Failure, void>> addTrackToPlaylist(String playlistId, String trackId) => 
    _storage.addTrackToPlaylist(playlistId, trackId);
  Future<Either<Failure, void>> removeTrackFromPlaylist(String playlistId, String trackId) => 
    _storage.removeTrackFromPlaylist(playlistId, trackId);
  Future<Either<Failure, void>> reorderPlaylistTracks(String playlistId, int oldIndex, int newIndex) => 
    _storage.reorderPlaylistTracks(playlistId, oldIndex, newIndex);

  // Liked
  Future<Either<Failure, List<String>>> getLikedTrackIds() => _storage.getLikedTrackIds();
  Future<Either<Failure, void>> toggleLiked(String trackId, bool isLiked) => 
    isLiked ? _storage.removeLikedTrack(trackId) : _storage.addLikedTrack(trackId);

  // History
  Future<Either<Failure, List<String>>> getHistoryTrackIds() => _storage.getHistoryTrackIds();
  Future<Either<Failure, void>> addToHistory(String trackId) => _storage.addToHistory(trackId);
  Future<Either<Failure, void>> clearHistory() => _storage.clearHistory();
}
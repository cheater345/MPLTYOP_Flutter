import 'package:freezed_annotation/freezed_annotation.dart';
import 'playlist.dart';

part 'library_state.freezed.dart';

@freezed
class LibraryState with _$LibraryState {
  const factory LibraryState({
    @Default([]) List<Playlist> playlists,
    @Default([]) List<String> likedTrackIds,
    @Default([]) List<String> historyTrackIds,
    @Default(false) bool isLoading,
    String? error,
  }) = _LibraryState;
  
  const LibraryState._();
  
  bool isLiked(String trackId) => likedTrackIds.contains(trackId);
  bool isInHistory(String trackId) => historyTrackIds.contains(trackId);
}
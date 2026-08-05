import 'package:freezed_annotation/freezed_annotation.dart';
import 'track.dart';

part 'playback_state.freezed.dart';

@freezed
class PlaybackState with _$PlaybackState {
  const factory PlaybackState({
    @Default(false) bool isPlaying,
    @Default(Duration.zero) Duration position,
    @Default(Duration.zero) Duration duration,
    @Default(0.8) double volume,
    @Default(RepeatMode.off) RepeatMode repeatMode,
    @Default(false) bool shuffle,
    Track? currentTrack,
    @Default([]) List<Track> queue,
    @Default(-1) int currentIndex,
    @Default(0.0) double bufferedPosition,
    @Default(ProcessingState.idle) ProcessingState processingState,
  }) = _PlaybackState;

  bool get hasTrack => currentTrack != null;
  
  double get progress => duration.inMilliseconds > 0 
    ? position.inMilliseconds / duration.inMilliseconds 
    : 0.0;
}
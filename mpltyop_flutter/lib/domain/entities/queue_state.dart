import 'package:freezed_annotation/freezed_annotation.dart';
import 'track.dart';

part 'queue_state.freezed.dart';

@freezed
class QueueState with _$QueueState {
  const factory QueueState({
    @Default([]) List<Track> queue,
    @Default(-1) int currentIndex,
    @Default(RepeatMode.off) RepeatMode repeatMode,
    @Default(false) bool shuffle,
    @Default(0.8) double volume,
  }) = _QueueState;

  Track? get currentTrack => 
    currentIndex >= 0 && currentIndex < queue.length ? queue[currentIndex] : null;
  
  bool get hasNext => currentIndex + 1 < queue.length;
  bool get hasPrevious => currentIndex > 0;
  
  QueueState next() => copyWith(
    currentIndex: (currentIndex + 1).clamp(0, queue.length - 1),
  );
  
  QueueState previous() => copyWith(
    currentIndex: (currentIndex - 1).clamp(0, queue.length - 1),
  );
  
  QueueState add(Track track, {bool playNext = false}) {
    if (playNext && currentIndex >= 0) {
      final newQueue = [...queue];
      newQueue.insert(currentIndex + 1, track);
      return copyWith(queue: newQueue);
    }
    return copyWith(queue: [...queue, track]);
  }
  
  QueueState removeAt(int index) {
    final newQueue = [...queue];
    newQueue.removeAt(index);
    int newCurrentIndex = currentIndex;
    if (index < currentIndex) newCurrentIndex--;
    if (newCurrentIndex >= newQueue.length) newCurrentIndex = newQueue.length - 1;
    return copyWith(queue: newQueue, currentIndex: newCurrentIndex);
  }
  
  QueueState move(int oldIndex, int newIndex) {
    final newQueue = [...queue];
    final item = newQueue.removeAt(oldIndex);
    newQueue.insert(newIndex, item);
    int newCurrentIndex = currentIndex;
    if (oldIndex == currentIndex) newCurrentIndex = newIndex;
    else if (oldIndex < currentIndex && newIndex >= currentIndex) newCurrentIndex--;
    else if (oldIndex > currentIndex && newIndex <= currentIndex) newCurrentIndex++;
    return copyWith(queue: newQueue, currentIndex: newCurrentIndex);
  }
  
  QueueState shuffleQueue() {
    if (queue.length <= 1) return this;
    final newQueue = [...queue];
    newQueue.shuffle();
    return copyWith(queue: newQueue, currentIndex: newQueue.indexOf(currentTrack!));
  }
}
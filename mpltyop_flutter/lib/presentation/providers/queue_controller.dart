import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/usecases/manage_queue.dart';
import '../../domain/entities/queue_state.dart';
import '../../domain/entities/track.dart';
import '../../domain/entities/repeat_mode.dart';

class QueueController extends StateNotifier<QueueState> {
  final ManageQueue _manageQueue;
  final dynamic _playbackService; // PlaybackService

  QueueController(this._manageQueue, this._playbackService) 
    : super(QueueState(queue: [], currentIndex: -1));

  void addTrack(Track track, {bool playNext = false}) {
    state = state.add(track, playNext: playNext);
  }

  void removeAt(int index) {
    state = state.removeAt(index);
  }

  void clearQueue() {
    state = state.copyWith(queue: [], currentIndex: -1);
  }

  void playAt(int index) {
    if (index >= 0 && index < state.queue.length) {
      state = state.copyWith(currentIndex: index);
    }
  }

  void next() {
    state = state.next();
  }

  void previous() {
    state = state.previous();
  }

  void toggleShuffle() {
    if (state.shuffle) {
      // TODO: restore original order
    } else {
      state = state.shuffleQueue();
    }
    state = state.copyWith(shuffle: !state.shuffle);
  }

  void cycleRepeat() {
    final modes = RepeatMode.values;
    final current = modes.indexOf(state.repeatMode);
    final next = modes[(current + 1) % modes.length];
    state = state.copyWith(repeatMode: next);
  }

  void setVolume(double volume) {
    state = state.copyWith(volume: volume.clamp(0.0, 1.0));
  }
}
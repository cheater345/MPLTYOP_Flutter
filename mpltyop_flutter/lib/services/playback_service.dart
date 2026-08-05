import 'dart:async';

import 'package:audio_service/audio_service.dart';
import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';

import '../data/repositories/storage_repository.dart';
import '../data/repositories/music_repository.dart';
import '../domain/entities/track.dart';
import '../domain/entities/playback_state.dart';

class PlaybackService extends BackgroundAudioTask {
  final MusicRepository _musicRepository;
  final StorageRepository _storageRepository;
  
  final AudioPlayer _player = AudioPlayer();
  final StreamController<PlaybackState> _stateController = StreamController.broadcast();
  
  Track? _currentTrack;
  List<Track> _queue = [];
  int _currentIndex = -1;
  RepeatMode _repeatMode = RepeatMode.off;
  bool _shuffle = false;
  double _volume = 0.8;

  Stream<PlaybackState> get playbackStateStream => _stateController.stream;

  PlaybackService(this._musicRepository, this._storageRepository) {
    _initPlayer();
  }

  void _initPlayer() {
    _player.playerStateStream.listen((state) {
      _updateState(
        playing: state.playing,
        processingState: state.processingState,
      );
    });
    
    _player.positionStream.listen((position) {
      _updateState(position: position);
    });
    
    _player.durationStream.listen((duration) {
      _updateState(duration: duration);
    });
  }

  Future<void> _loadTrack(Track track) async {
    if (track.audioUrl != null && track.isAudioUrlValid) {
      await _player.setUrl(track.audioUrl!);
    } else {
      // Need to re-extract audio URL
      // TODO: implement re-extraction
    }
  }

  Future<void> playTrack(Track track, {List<Track>? queue, int? index}) async {
    _currentTrack = track;
    if (queue != null) {
      _queue = queue;
      _currentIndex = index ?? 0;
    }
    await _loadTrack(track);
    await _player.play();
    _emitState();
  }

  Future<void> playPause() async {
    if (_player.playing) {
      await _player.pause();
    } else {
      await _player.play();
    }
  }

  Future<void> next() async {
    if (_currentIndex + 1 < _queue.length) {
      _currentIndex++;
      await playTrack(_queue[_currentIndex]);
    }
  }

  Future<void> previous() async {
    if (_currentIndex > 0) {
      _currentIndex--;
      await playTrack(_queue[_currentIndex]);
    }
  }

  Future<void> seek(Duration position) async {
    await _player.seek(position);
  }

  Future<void> setVolume(double volume) async {
    _volume = volume.clamp(0.0, 1.0);
    await _player.setVolume(_volume);
  }

  Future<void> setRepeatMode(RepeatMode mode) async {
    // _repeatMode = mode;
    // _player.setLoopMode(_toLoopMode(mode));
  }

  Future<void> setShuffle(bool shuffle) async {
    // _shuffle = shuffle;
    // _player.setShuffleModeEnabled(shuffle);
  }

  void _updateState({
    bool? playing,
    Duration? position,
    Duration? duration,
    ProcessingState? processingState,
  }) {
    _stateController.add(PlaybackState(
      isPlaying: playing ?? _player.playing,
      position: position ?? _player.position,
      duration: duration ?? _player.duration ?? Duration.zero,
      volume: _volume,
      repeatMode: _repeatMode,
      shuffle: _shuffle,
      currentTrack: _currentTrack,
      queue: _queue,
      currentIndex: _currentIndex,
      processingState: processingState ?? _player.processingState,
    ));
  }

  void _emitState() {
    _stateController.add(PlaybackState(
      isPlaying: _player.playing,
      position: _player.position,
      duration: _player.duration ?? Duration.zero,
      volume: _volume,
      repeatMode: _repeatMode,
      shuffle: _shuffle,
      currentTrack: _currentTrack,
      queue: _queue,
      currentIndex: _currentIndex,
      processingState: _player.processingState,
    ));
  }

  @override
  Future<void> onStart(Map<String, dynamic>? params) async {
    await _player.setVolume(_volume);
  }

  @override
  Future<void> onStop() async {
    await _player.stop();
  }

  @override
  Future<void> onPause() async {
    await _player.pause();
  }

  @override
  Future<void> onPlay() async {
    await _player.play();
  }

  @override
  Future<void> onSkipToNext() async {
    await next();
  }

  @override
  Future<void> onSkipToPrevious() async {
    await previous();
  }

  @override
  Future<void> onSeekTo(Duration position) async {
    await seek(position);
  }

  @override
  Future<void> onCustomAction(String name, dynamic arguments) async {
    switch (name) {
      case 'set_volume':
        await setVolume(arguments['volume'] as double);
        break;
      case 'set_repeat':
        // await setRepeatMode(arguments['mode'] as RepeatMode);
        break;
      case 'set_shuffle':
        // await setShuffle(arguments['shuffle'] as bool);
        break;
    }
  }

  @override
  Future<void> dispose() async {
    await _player.dispose();
    await _stateController.close();
    await super.dispose();
  }
}
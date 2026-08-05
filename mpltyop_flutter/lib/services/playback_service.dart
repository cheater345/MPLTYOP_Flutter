import 'dart:async';

import 'package:audio_service/audio_service.dart';
import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';

import '../data/repositories/storage_repository.dart';
import '../data/repositories/music_repository.dart';
import '../domain/entities/track.dart';
import '../domain/entities/repeat_mode.dart';
import '../domain/entities/playback_state.dart' as mpl;

class PlaybackService extends BaseAudioHandler {
  final MusicRepository? _musicRepository;
  final StorageRepository? _storageRepository;
  
  final AudioPlayer _player = AudioPlayer();
  final StreamController<mpl.PlaybackState> _stateController = StreamController.broadcast();
  
  Track? _currentTrack;
  List<Track> _queue = [];
  int _currentIndex = -1;
  RepeatMode _repeatMode = RepeatMode.off;
  bool _shuffle = false;
  double _volume = 0.8;

  Stream<mpl.PlaybackState> get playbackStateStream => _stateController.stream;

  PlaybackService({MusicRepository? musicRepository, StorageRepository? storageRepository})
      : _musicRepository = musicRepository,
        _storageRepository = storageRepository {
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

  @override
  Future<void> play() async {
    await _player.play();
  }

  @override
  Future<void> pause() async {
    await _player.pause();
  }

  @override
  Future<void> stop() async {
    await _player.stop();
  }

  @override
  Future<void> seek(Duration position) async {
    await _player.seek(position);
  }

  @override
  Future<void> skipToNext() async {
    await next();
  }

  @override
  Future<void> skipToPrevious() async {
    await previous();
  }

  @override
  Future<void> customAction(String name, [Map<String, dynamic>? extras]) async {
    switch (name) {
      case 'set_volume':
        await setVolume(extras?['volume'] as double? ?? _volume);
        break;
    }
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

  Future<void> setVolume(double volume) async {
    _volume = volume.clamp(0.0, 1.0);
    await _player.setVolume(_volume);
  }

  Future<void> updateRepeatMode(RepeatMode mode) async {
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
    _stateController.add(mpl.PlaybackState(
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
    _stateController.add(mpl.PlaybackState(
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

  Future<void> dispose() async {
    await _player.dispose();
    await _stateController.close();
  }
}
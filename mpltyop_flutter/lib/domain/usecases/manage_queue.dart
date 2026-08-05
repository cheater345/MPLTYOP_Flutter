import '../../data/repositories/storage_repository.dart';
import '../entities/track.dart';
import '../entities/queue_state.dart';
import '../entities/playlist.dart';
import '../../core/errors.dart';

class ManageQueue {
  final StorageRepository _storage;
  final dynamic _playbackService; // PlaybackService

  ManageQueue(this._storage, this._playbackService);

  Future<Either<Failure, QueueState>> loadQueue() async {
    final trackIdsResult = await _storage.getQueueTrackIds();
    final prefs = await _playbackService._prefs; // TODO: fix
    final currentIndex = 0; // TODO: load from prefs
    
    if (trackIdsResult.isLeft()) return Left(trackIdsResult.errorOrNull!);
    
    // TODO: load tracks from cache
    return Right(QueueState(queue: [], currentIndex: 0));
  }

  Future<Either<Failure, void>> saveQueue(QueueState state) {
    return _storage.saveQueue(state.queue.map((t) => t.id).toList(), state.currentIndex);
  }

  Future<Either<Failure, void>> addTrack(Track track, {bool playNext = false}) {
    // TODO: implement
    return Right(null);
  }

  Future<Either<Failure, void>> removeTrack(int index) {
    return Right(null);
  }

  Future<Either<Failure, void>> clearQueue() {
    return Right(null);
  }
}
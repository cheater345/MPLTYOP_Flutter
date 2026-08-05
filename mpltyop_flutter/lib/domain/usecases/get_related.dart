import 'package:dartz/dartz.dart';
import '../../data/repositories/music_repository.dart';
import '../entities/track.dart';
import '../../core/errors.dart';

class GetRelated {
  final MusicRepository _repository;

  GetRelated(this._repository);

  Future<Either<Failure, List<Track>>> call(String videoId, {int limit = 20}) {
    return _repository.getRelatedTracks(videoId);
  }
}
import 'package:dartz/dartz.dart';
import '../../data/repositories/music_repository.dart';
import '../../core/errors.dart';

class GetLyrics {
  final MusicRepository _repository;

  GetLyrics(this._repository);

  Future<Either<Failure, String?>> call(String videoId) {
    return _repository.getLyrics(videoId);
  }
}
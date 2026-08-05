import '../repositories/music_repository.dart';
import '../entities/track.dart';
import '../../core/errors.dart';

class SearchTracks {
  final MusicRepository _repository;

  SearchTracks(this._repository);

  Future<Either<Failure, List<Track>>> call(String query, {int? limit}) {
    return _repository.searchTracks(query, limit: limit);
  }
}
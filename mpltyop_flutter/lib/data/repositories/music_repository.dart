import 'package:dartz/dartz.dart';
import '../../core/errors.dart';
import '../../core/constants.dart';
import '../datasources/yt_music_api.dart';
import '../../domain/entities/track.dart';
import 'storage_repository.dart';

abstract class MusicRepository {
  Future<Either<Failure, List<Track>>> searchTracks(String query, {int? limit});
  Stream<Either<Failure, Track>> streamSearch(String query, {int? limit});
  Future<Either<Failure, List<Track>>> getRelatedTracks(String videoId);
  Future<Either<Failure, String?>> getLyrics(String videoId);
  Future<Either<Failure, Track?>> getTrackDetails(String videoId);
}

class MusicRepositoryImpl implements MusicRepository {
  final YtMusicApi _api;
  final StorageRepository _storage;

  MusicRepositoryImpl(this._api, this._storage);

  @override
  Future<Either<Failure, List<Track>>> searchTracks(String query, {int? limit}) async {
    try {
      final tracks = await _api.search(query: query, limit: limit ?? AppConstants.defaultSearchLimit);
      return Right(tracks);
    } catch (e) {
      return Left(Failure.extractionFailed(e.toString()));
    }
  }

  @override
  Stream<Either<Failure, Track>> streamSearch(String query, {int? limit}) async* {
    try {
      await for (final track in _api.streamSearch(query: query, limit: limit ?? AppConstants.defaultSearchLimit)) {
        yield Right(track);
      }
    } catch (e) {
      yield Left(Failure.extractionFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, List<Track>>> getRelatedTracks(String videoId) async {
    try {
      final tracks = await _api.getRelated(videoId: videoId);
      return Right(tracks);
    } catch (e) {
      return Left(Failure.extractionFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, String?>> getLyrics(String videoId) async {
    try {
      final lyrics = await _api.getLyrics(videoId);
      return Right(lyrics);
    } catch (e) {
      return Left(Failure.extractionFailed(e.toString()));
    }
  }

  @override
  Future<Either<Failure, Track?>> getTrackDetails(String videoId) async {
    try {
      final track = await _api.getTrackDetails(videoId);
      return Right(track);
    } catch (e) {
      return Left(Failure.extractionFailed(e.toString()));
    }
  }
}
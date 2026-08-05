import 'package:freezed_annotation/freezed_annotation.dart';

part 'errors.freezed.dart';

@freezed
sealed class Failure with _$Failure {
  const factory Failure.network(String message) = NetworkFailure;
  const factory Failure.rateLimited({Duration? retryAfter}) = RateLimitedFailure;
  const factory Failure.extractionFailed(String detail) = ExtractionFailure;
  const factory Failure.playbackFailed(String detail) = PlaybackFailure;
  const factory Failure.storageFailed(String detail) = StorageFailure;
  const factory Failure.permissionDenied(String permission) = PermissionFailure;
  const factory Failure.unknown(String message) = UnknownFailure;
}

extension FailureExt on Failure {
  String get userMessage => switch (this) {
    NetworkFailure(:final message) => 'Network error: $message',
    RateLimitedFailure(:final retryAfter) => 
      'Rate limited. Retry in ${retryAfter?.inSeconds ?? 30}s',
    ExtractionFailure(:final detail) => 'Could not extract audio: $detail',
    PlaybackFailure(:final detail) => 'Playback error: $detail',
    StorageFailure(:final detail) => 'Storage error: $detail',
    PermissionDenied(:final permission) => 'Permission denied: $permission',
    UnknownFailure(:final message) => 'Error: $message',
  };
}

@freezed
sealed class Result<T, E extends Failure> with _$Result<T, E> {
  const factory Result.success(T value) = Success<T, E>;
  const factory Result.failure(E error) = FailureResult<T, E>;
}

extension ResultExt<T, E extends Failure> on Result<T, E> {
  bool get isSuccess => this is Success<T, E>;
  bool get isFailure => this is FailureResult<T, E>;
  
  T? get valueOrNull => switch (this) {
    Success<T, E>(:final value) => value,
    _ => null,
  };
  
  E? get errorOrNull => switch (this) {
    FailureResult<T, E>(:final error) => error,
    _ => null,
  };
}
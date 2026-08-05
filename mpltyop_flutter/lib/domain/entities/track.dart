import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:hive/hive.dart';
import 'package:collection/collection.dart';

part 'track.freezed.dart';
part 'track.g.dart';

@freezed
@HiveType(typeId: 0, adapterName: 'TrackAdapter')
class Track with _$Track {
  const Track._();
  
  const factory Track({
    @HiveField(0) required String id,
    @HiveField(1) required String title,
    @HiveField(2) required String artist,
    @HiveField(3) required int duration,
    @HiveField(4) required String thumbnailUrl,
    @HiveField(5) String? audioUrl,
    @HiveField(6) DateTime? audioUrlExpiry,
    @HiveField(7) String? lyrics,
    @HiveField(8) String? album,
    @HiveField(9) String? artistId,
    @HiveField(10) int? year,
    @HiveField(11) @Default(false) bool isExplicit,
  }) = _Track;

  factory Track.fromJson(Map<String, dynamic> json) => _$TrackFromJson(json);
  
  factory Track.fromYtResult(Map<String, dynamic> result) {
    return Track(
      id: result['videoId'] ?? result['id'] ?? '',
      title: result['title'] ?? 'Unknown Title',
      artist: (result['artists'] as List?)?.firstOrNull?['name'] ?? result['artist'] ?? 'Unknown Artist',
      duration: result['duration'] ?? 0,
      thumbnailUrl: (result['thumbnails'] as List?)?.lastOrNull?['url'] 
          ?? result['thumbnailUrl'] 
          ?? 'https://i.ytimg.com/vi/${result['videoId'] ?? result['id']}/hqdefault.jpg',
      audioUrl: result['audioUrl'],
      album: result['album']?['name'],
      artistId: result['artistId'],
      year: result['year'],
      isExplicit: result['isExplicit'] ?? false,
    );
  }
  
  String get formattedDuration {
    final m = duration ~/ 60;
    final s = duration % 60;
    return '$m:${s.toString().padLeft(2, '0')}';
  }
  
  bool get isAudioUrlValid => audioUrl != null && 
    audioUrlExpiry != null && 
    DateTime.now().isBefore(audioUrlExpiry!);
}
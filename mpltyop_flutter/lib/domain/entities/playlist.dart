import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:hive/hive.dart';
import 'track.dart';

part 'playlist.freezed.dart';
part 'playlist.g.dart';

@freezed
@HiveType(typeId: 1, adapterName: 'PlaylistAdapter')
class Playlist with _$Playlist {
  const Playlist._();
  
  const factory Playlist({
    @HiveField(0) required String id,
    @HiveField(1) required String name,
    @HiveField(2) @Default([]) List<String> trackIds,
    @HiveField(3) required DateTime createdAt,
    @HiveField(4) DateTime? updatedAt,
    @HiveField(5) String? description,
    @HiveField(6) String? coverUrl,
  }) = _Playlist;

  factory Playlist.fromJson(Map<String, dynamic> json) => _$PlaylistFromJson(json);
  
  int get trackCount => trackIds.length;
  
  Playlist copyWithTracks(List<String> newTrackIds) => copyWith(
    trackIds: newTrackIds,
    updatedAt: DateTime.now(),
  );
}
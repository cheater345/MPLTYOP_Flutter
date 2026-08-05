import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:hive/hive.dart';

part 'artist.freezed.dart';
part 'artist.g.dart';

@freezed
@HiveType(typeId: 2, adapterName: 'ArtistAdapter')
class Artist with _$Artist {
  const Artist._();
  
  const factory Artist({
    @HiveField(0) required String id,
    @HiveField(1) required String name,
    @HiveField(2) String? thumbnailUrl,
    @HiveField(3) int? subscriberCount,
    @HiveField(4) String? description,
  }) = _Artist;

  factory Artist.fromJson(Map<String, dynamic> json) => _$ArtistFromJson(json);
}
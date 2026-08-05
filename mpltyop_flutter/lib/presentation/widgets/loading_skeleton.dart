import 'package:flutter/material.dart';
import 'package:shimmer/shimmer.dart';

class TrackCardSkeleton extends StatelessWidget {
  const TrackCardSkeleton({super.key});

  @override
  Widget build(BuildContext context) {
    return Shimmer.fromColors(
      baseColor: Colors.grey[800]!,
      highlightColor: Colors.grey[700]!,
      child: SizedBox(
        width: 160,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              width: 160,
              height: 160,
              decoration: BoxDecoration(
                color: Colors.grey[800],
                borderRadius: BorderRadius.circular(8),
              ),
            ),
            const SizedBox(height: 8),
            Container(height: 16, width: 120, color: Colors.grey[800]),
            const SizedBox(height: 4),
            Container(height: 12, width: 80, color: Colors.grey[700]),
          ],
        ),
      ),
    );
  }
}

class TrackTileSkeleton extends StatelessWidget {
  const TrackTileSkeleton({super.key});

  @override
  Widget build(BuildContext context) {
    return Shimmer.fromColors(
      baseColor: Colors.grey[800]!,
      highlightColor: Colors.grey[700]!,
      child: ListTile(
        leading: Container(width: 48, height: 48, color: Colors.grey[800]),
        title: Container(height: 16, width: 150, color: Colors.grey[800]),
        subtitle: Container(height: 12, width: 100, color: Colors.grey[700]),
      ),
    );
  }
}

class PlaylistCardSkeleton extends StatelessWidget {
  const PlaylistCardSkeleton({super.key});

  @override
  Widget build(BuildContext context) {
    return Shimmer.fromColors(
      baseColor: Colors.grey[800]!,
      highlightColor: Colors.grey[700]!,
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.grey[900],
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            AspectRatio(
              aspectRatio: 1,
              child: Container(
                decoration: BoxDecoration(
                  color: Colors.grey[800],
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
            ),
            const SizedBox(height: 12),
            Container(height: 16, width: 100, color: Colors.grey[800]),
            const SizedBox(height: 4),
            Container(height: 12, width: 60, color: Colors.grey[700]),
          ],
        ),
      ),
    );
  }
}

class SectionSkeleton extends StatelessWidget {
  final int itemCount;
  const SectionSkeleton({super.key, this.itemCount = 6});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 180,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        itemCount: itemCount,
        separatorBuilder: (_, __) => const SizedBox(width: 12),
        itemBuilder: (_, __) => const TrackCardSkeleton(),
      ),
    );
  }
}
package com.sksamuel.elastic4s.search

import com.sksamuel.elastic4s.testkit.ElasticSugar
import org.scalatest.{Matchers, WordSpec}

class ScrollTest extends WordSpec with Matchers with ElasticSugar {

  client.execute {
    bulk(
      indexInto("katebush/songs").fields("name" -> "hounds of love", "year" -> "1985"),
      indexInto("katebush/songs").fields("name" -> "top of the city", "year" -> "1985"),
      indexInto("katebush/songs").fields("name" -> "wuthering heights", "year" -> "1979"),
      indexInto("katebush/songs").fields("name" -> "dream of sheep", "year" -> "1985"),
      indexInto("katebush/songs").fields("name" -> "waking the watch", "year" -> "1985"),
      indexInto("katebush/songs").fields("name" -> "watching you wathing me", "year" -> "1985"),
      indexInto("katebush/songs").fields("name" -> "cloudbusting", "year" -> "1985"),
      indexInto("katebush/songs").fields("name" -> "under ice", "year" -> "1985"),
      indexInto("katebush/songs").fields("name" -> "jig of life", "year" -> "1985"),
      indexInto("katebush/songs").fields("name" -> "hello earth", "year" -> "1985")
    )
  }.await

  refresh("katebush")
  blockUntilCount(5, "katebush")

  "a scroll" should {
    "return all results" in {

      val resp1 = client.execute {
        searchIn("katebush" / "songs").query("1985").scroll("1m").limit(2).sort(fieldSort("name"))
      }.await
      resp1.hits.map(_.field("name")).toList shouldBe ""

      val resp2 = client.execute {
        searchScroll(resp1.scrollId).keepAlive("1m")
      }.await
      resp2.hits.map(_.field("name")).toList shouldBe ""

      val resp3 = client.execute {
        searchScroll(resp2.scrollId).keepAlive("1m")
      }.await
      resp3.hits.map(_.field("name")).toList shouldBe ""

      val resp4 = client.execute {
        searchScroll(resp3.scrollId).keepAlive("1m")
      }.await
      resp4.hits.map(_.field("name")).toList shouldBe ""

      val resp5 = client.execute {
        searchScroll(resp4.scrollId)
      }.await
      resp5.hits.map(_.field("name")).toList shouldBe ""
    }
  }
}
package com.jakewharton.mosaic

import androidx.compose.runtime.Applier
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NodeApplierTest {
	private val root = BoxNode()
	private val applier = MosaicNodeApplier(root)

	private fun <T> Applier<T>.insert(index: Int, instance: T) {
		insertBottomUp(index, instance)
	}

	@Test fun insertAtEnd() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(1, two)
		val three = TextNode("three")
		applier.insert(2, three)
		assertChildren(one, two, three)
	}

	@Test fun insertAtStart() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(0, two)
		val three = TextNode("three")
		applier.insert(0, three)
		assertChildren(three, two, one)
	}

	@Test fun insertAtMiddle() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(1, two)
		val three = TextNode("three")
		applier.insert(1, three)
		assertChildren(one, three, two)
	}

	@Test fun removeSingleAtEnd() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(1, two)
		val three = TextNode("three")
		applier.insert(2, three)

		applier.remove(2, 1)
		assertChildren(one, two)
	}

	@Test fun removeSingleAtStart() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(1, two)
		val three = TextNode("three")
		applier.insert(2, three)

		applier.remove(0, 1)
		assertChildren(two, three)
	}

	@Test fun removeSingleInMiddle() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(1, two)
		val three = TextNode("three")
		applier.insert(2, three)

		applier.remove(1, 1)
		assertChildren(one, three)
	}

	@Test fun removeMultipleAtEnd() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(1, two)
		val three = TextNode("three")
		applier.insert(2, three)

		applier.remove(1, 2)
		assertChildren(one)
	}

	@Test fun removeMultipleAtStart() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(1, two)
		val three = TextNode("three")
		applier.insert(2, three)

		applier.remove(0, 2)
		assertChildren(three)
	}

	@Test fun removeMultipleInMiddle() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(1, two)
		val three = TextNode("three")
		applier.insert(2, three)
		val four = TextNode("four")
		applier.insert(3, four)

		applier.remove(1, 2)
		assertChildren(one, four)
	}

	@Test fun removeAll() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(1, two)
		val three = TextNode("three")
		applier.insert(2, three)

		applier.remove(0, 3)
		assertChildren()
	}

	@Test fun moveSingleLower() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(1, two)
		val three = TextNode("three")
		applier.insert(2, three)

		applier.move(2, 0, 1)
		assertChildren(three, one, two)
	}

	@Test fun moveSingleHigher() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(1, two)
		val three = TextNode("three")
		applier.insert(2, three)

		applier.move(0, 2, 1)
		assertChildren(two, one, three)
	}

	@Test fun moveMultipleLower() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(1, two)
		val three = TextNode("three")
		applier.insert(2, three)
		val four = TextNode("four")
		applier.insert(3, four)

		applier.move(2, 0, 2)
		assertChildren(three, four, one, two)
	}

	@Test fun moveMultipleHigher() {
		val one = TextNode("one")
		applier.insert(0, one)
		val two = TextNode("two")
		applier.insert(1, two)
		val three = TextNode("three")
		applier.insert(2, three)
		val four = TextNode("four")
		applier.insert(3, four)

		applier.move(0, 4, 2)
		assertChildren(three, four, one, two)
	}

	private fun assertChildren(vararg nodes: MosaicNode) {
		assertThat(root.children).containsExactlyElementsIn(nodes).inOrder()
	}
}

package org.openrefine.browsing.util;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class NumericFacetStateTests {

	NumericFacetState multiValuesState;
	NumericFacetState emptyState;
	NumericFacetState singleValueState;
	
	@BeforeClass
	public void setUpFacetState() {
		multiValuesState = new NumericFacetState(
				12, 3, 4, 5, -1, -3,
				new long[] { 3, 4, 0, 0, 5 }
				);
		emptyState = new NumericFacetState(
				0, 3, 4, 5, 0.0);
		singleValueState = new NumericFacetState(
				2, 3, 4, 5, 3456.7);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testRescaleFiner() {
		multiValuesState.rescale(-2);
	}
	
	@Test
	public void testRescaleIdentical() {
		Assert.assertEquals(multiValuesState.rescale(-1), multiValuesState);
	}
	
	@Test
	public void testRescaleCoarser() {
		NumericFacetState rescaled = multiValuesState.rescale(0);
		
		Assert.assertEquals(rescaled.getLogBinSize(), 0);
		Assert.assertEquals(rescaled.getBlankCount(), multiValuesState.getBlankCount());
		Assert.assertEquals(rescaled.getErrorCount(), multiValuesState.getErrorCount());
		Assert.assertEquals(rescaled.getNumericCount(), multiValuesState.getNumericCount());
		Assert.assertEquals(rescaled.getNonNumericCount(), multiValuesState.getNonNumericCount());
		Assert.assertEquals(rescaled.getMinBin(), -1);
		Assert.assertEquals(rescaled.getBins(), new long[] { 7, 5 });
	}
	
	@Test
	public void testEmptyState() {
		Assert.assertNull(emptyState.getBins());
		
		// rescaling an empty facet state does not do anything
		NumericFacetState rescaled = emptyState.rescale(3);
		
		Assert.assertEquals(rescaled.getLogBinSize(), 0);
	}
	
	@Test
	public void testSingleValueState() {
		Assert.assertNull(singleValueState.getBins());
		
		// rescaling a facet with at least one value creates explicit bins
		NumericFacetState rescaled = singleValueState.rescale(3);
		
		Assert.assertEquals(rescaled.getLogBinSize(), 3);
		Assert.assertEquals(rescaled.getBins(), new long[] { 2 });
		Assert.assertEquals(rescaled.getMinBin(), 3);
		Assert.assertEquals(rescaled.getNumericCount(), 2);
		
		// it does so even if the new scale is 0 (internally the default)
		rescaled = singleValueState.rescale(0);
		Assert.assertEquals(rescaled.getLogBinSize(), 0);
		Assert.assertEquals(rescaled.getBins(), new long[] { 2 });
		Assert.assertEquals(rescaled.getMinBin(), 3456);
		Assert.assertEquals(rescaled.getNumericCount(), 2);
	}
	
	@Test
	public void testIncrementNonNumeric() {
		NumericFacetState incremented = emptyState.addCounts(1, 0, 0);
		Assert.assertEquals(incremented.getNonNumericCount(), emptyState.getNonNumericCount() + 1);
		Assert.assertNull(incremented.getBins());
		
		incremented = multiValuesState.addCounts(1, 0, 0);
		Assert.assertEquals(incremented.getNonNumericCount(), multiValuesState.getNonNumericCount() + 1);
		Assert.assertNotNull(incremented.getBins());
	}
	
	@Test
	public void testIncrementError() {
		NumericFacetState incremented = emptyState.addCounts(0, 1, 0);
		Assert.assertEquals(incremented.getErrorCount(), emptyState.getErrorCount() + 1);
		Assert.assertNull(incremented.getBins());
		
		incremented = multiValuesState.addCounts(0, 1, 0);
		Assert.assertEquals(incremented.getErrorCount(), multiValuesState.getErrorCount() + 1);
		Assert.assertNotNull(incremented.getBins());
	}
	
	@Test
	public void testIncrementBlank() {
		NumericFacetState incremented = emptyState.addCounts(0, 0, 1);
		Assert.assertEquals(incremented.getBlankCount(), emptyState.getBlankCount() + 1);
		Assert.assertNull(incremented.getBins());
		
		incremented = multiValuesState.addCounts(0, 0, 1);
		Assert.assertEquals(incremented.getBlankCount(), multiValuesState.getBlankCount() + 1);
		Assert.assertNotNull(incremented.getBins());
	}
	
	@Test
	public void testEquals() {
		Assert.assertFalse(emptyState.equals(67));
		Assert.assertFalse(emptyState.equals(multiValuesState));
		Assert.assertTrue(emptyState.equals(emptyState));
		Assert.assertEquals(emptyState.hashCode(), emptyState.hashCode());
	}
	
	@Test
	public void testToString() {
		Assert.assertTrue(emptyState.toString().contains("numeric"));
	}
}

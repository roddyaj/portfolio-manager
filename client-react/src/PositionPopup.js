import Popover from 'react-bootstrap/Popover';

export function renderPositionPopup(position) {
	return (
		<Popover id={`pospop-openorder-${position.symbol}`} style={{ maxWidth: 400 }}>
			<Popover.Header as="h3">{position.description}</Popover.Header>
			<Popover.Body>
				{renderStatistics(position)}
				{renderOpenOrders(position)}
				{renderTransactions(position)}
			</Popover.Body>
		</Popover>
	);
}

function renderStatistics(position) {
	const low = position['52WeekLow'];
	const high = position['52WeekHigh'];
	const priceRangePct = 100 * (position.price - low) / (high - low);

	return (
		<div>
			<span style={{ fontWeight: "bold" }}>Statistics</span>
			<table>
				<tbody>
					<tr><td className="l">52 week range:</td><td className="l">{`${low} - ${high} (${priceRangePct.toFixed(1)}%)`}</td></tr>
					{position.dividendYield && <tr><td className="l">Dividend yield:</td><td className="l">{`${position.dividendYield}%`}</td></tr>}
					{position.peRatio && <tr><td className="l">P/E ratio:</td><td className="l">{position.peRatio}</td></tr>}
				</tbody>
			</table>
		</div>
	);
}

function renderOpenOrders(position) {
	if (!position.openOrders)
		return null;

	const sortedOrders = position.openOrders.sort((a, b) => b.limitPrice - a.limitPrice);
	const rows = [];
	rows.push(...sortedOrders.filter(o => o.action === 'Sell').map((order, i) => (
		<tr key={`pospop-opensell-${position.symbol}-${i}`}>
			<td>{order.action}</td>
			<td>{order.quantity}</td>
			<td>@</td>
			<td>{order.limitPrice.toFixed(2)}</td>
		</tr>
	)));
	rows.push((
		<tr key={`pospop-openorder-${position.symbol}-price`}>
			<td className="l" colSpan={3}>Current</td>
			<td>{position.price.toFixed(2)}</td>
		</tr>
	));
	rows.push(...sortedOrders.filter(o => o.action === 'Buy').map((order, i) => (
		<tr key={`pospop-openbuy-${position.symbol}-${i}`}>
			<td>{order.action}</td>
			<td>{order.quantity}</td>
			<td>@</td>
			<td>{order.limitPrice.toFixed(2)}</td>
		</tr>
	)));
	return (
		<div style={{ marginTop: 16 }}>
			<span style={{ fontWeight: "bold" }}>Open Orders</span>
			<table>
				<tbody>
					{rows}
				</tbody>
			</table>
		</div>
	);
}

function renderTransactions(position) {
	if (!position.transactions)
		return null;

	const rows = position.transactions.slice(0, 10).map((transactions, i) => (
		<tr key={`pospop-transaction-${position.symbol}-${i}`}>
			<td>{transactions.date}</td>
			<td>{transactions.action}</td>
			<td>{transactions.quantity}</td>
			<td>{transactions.price.toFixed(2)}</td>
		</tr>
	));
	return (
		<div style={{ marginTop: 16 }}>
			<span style={{ fontWeight: "bold" }}>Transactions</span>
			<table>
				<tbody>
					{rows}
				</tbody>
			</table>
		</div>
	);
}

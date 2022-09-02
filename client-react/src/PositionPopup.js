import Popover from 'react-bootstrap/Popover';

export function renderPositionPopup(position) {
	return (
		<Popover id={`pospop-openorder-${position.symbol}`} style={{ maxWidth: 400, font: "14px Arial, sans-serif" }}>
			<Popover.Header as="h3">{position.symbol} - {position.description}</Popover.Header>
			<Popover.Body>
				{renderPriceInfo(position)}
				{renderStatistics(position)}
				{renderOpenOrders(position)}
				{renderOptions(position)}
				{renderTransactions(position)}
			</Popover.Body>
		</Popover>
	);
}

function renderPriceInfo(position) {
	const gainLoss = `${position.gainLossPct >= 0 ? "+" : ""}${position.gainLossPct.toFixed(2)}%`;
	const dayChange = `${position.dayChangePct >= 0 ? "+" : ""}${position.dayChangePct.toFixed(2)}%`;
	return (
		<div>
			<table>
				<tbody>
					<tr style={{ fontSize: 16 }}>
						<td>{position.quantity}</td>
						<td>shares x</td>
						<td>{position.price.toFixed(2)}</td>
						<td>=</td>
						<td>{position.marketValue.toFixed(2)}</td>
						<td rowSpan={2} style={{ color: position.gainLossPct >= 0 ? "green" : "#C00", paddingLeft: 8 }}>{gainLoss}</td>
					</tr>
					<tr style={{ fontSize: 12, backgroundColor: "revert" }}>
						<td></td>
						<td></td>
						<td className="c">({(position.costBasis / position.quantity).toFixed(2)})</td>
						<td></td>
						<td className="c">({position.costBasis.toFixed(2)})</td>
					</tr>
				</tbody>
			</table>
			<div style={{ marginTop: 6, marginLeft: 1 }}>Day: <span style={{ color: position.dayChangePct >= 0 ? "green" : "#C00" }}>{dayChange}</span></div>
		</div>
	);
}

function renderStatistics(position) {
	const low = position['52WeekLow'];
	const high = position['52WeekHigh'];
	const priceRangePct = 100 * (position.price - low) / (high - low);

	return (
		<div style={{ marginTop: 16 }}>
			<div style={{ fontWeight: "bold", marginBottom: 2 }}>Statistics</div>
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
			<div style={{ fontWeight: "bold", marginBottom: 2 }}>Open Orders</div>
			<table>
				<tbody>
					{rows}
				</tbody>
			</table>
		</div>
	);
}

function renderOptions(position) {
	if (!position.options)
		return null;

	const rows = position.options.filter(o => o.quantity < 0).map((option, i) => {
		const [symbol, expiry, strike, type] = option.symbol.split(" ");
		return (
			<tr key={`pospop-option-${symbol}-${i}`}>
				<td>{Math.abs(option.quantity)}</td>
				<td>{type}</td>
				<td>{expiry}</td>
				<td>{strike}</td>
			</tr>
		);
	});
	return (
		<div style={{ marginTop: 16 }}>
			<div style={{ fontWeight: "bold", marginBottom: 2 }}>Short Options</div>
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

	const rows = position.transactions.slice(0, 10).map((transaction, i) => (
		<tr key={`pospop-transaction-${position.symbol}-${i}`}>
			<td>{transaction.date}</td>
			<td>{transaction.action}</td>
			<td>{transaction.quantity}</td>
			<td>{transaction.price.toFixed(2)}</td>
		</tr>
	));
	return (
		<div style={{ marginTop: 16 }}>
			<div style={{ fontWeight: "bold", marginBottom: 2 }}>Transactions</div>
			<table>
				<tbody>
					{rows}
				</tbody>
			</table>
		</div>
	);
}

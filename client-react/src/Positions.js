import { useState } from "react";
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';
import Popover from 'react-bootstrap/Popover';

function Positions(props) {
	const { portfolio } = props;

	const [showAllPositions, setShowAllPositions] = useState(true);

	const viewPositions = portfolio.positions
		.filter(p => !p.symbol.includes(" ") && (showAllPositions || p.sharesToBuy))
		.sort((a, b) => b.marketValue - a.marketValue);

	if (viewPositions.length === 0) {
		return null;
	}

	return (
		<div className="pm-block">
			<div className="pm-heading">
				<div className="pm-title">Positions ({viewPositions.length})</div>

				<div style={{ marginLeft: 12 }}>
					<input type="radio" name="positionVisibility" id="visibility-all" value="all" checked={showAllPositions} onChange={() => setShowAllPositions(true)} />
					<label htmlFor="visibility-all" style={{ padding: 2, marginRight: 4 }}>All</label>
					<input type="radio" name="positionVisibility" id="visibility-actions" value="actions" checked={!showAllPositions} onChange={() => setShowAllPositions(false)} />
					<label htmlFor="visibility-actions" style={{ padding: 2, marginRight: 4 }}>Actions</label>
				</div>
			</div>
			<table>
				<thead>
					<tr>
						<th className="l">Ticker</th>
						{showAllPositions && <th>#</th>}
						<th>Price</th>
						{showAllPositions && <th>Value</th>}
						<th>Day</th>
						{showAllPositions && <th>G/L</th>}
						{showAllPositions && <th>Actual</th>}
						{showAllPositions && <th>Target</th>}
						{showAllPositions && <th>Ratio</th>}
						<th className="l">Action</th>
						<th className="c">Open</th>
					</tr>
				</thead>
				<tbody>
					{viewPositions.map((p, i) => renderRow(p, i, showAllPositions))}
				</tbody>
			</table>
		</div>
	);
}

function renderRow(position, i, showAllPositions) {
	const action = position.sharesToBuy > 0 ? "Buy" : "Sell";
	const actionText = `${action} ${Math.abs(position.sharesToBuy)}`;
	const actionUrl = `https://client.schwab.com/Areas/Trade/Allinone/index.aspx?tradeaction=${action}&Symbol=${position.symbol}`;
	const openBuyCount = position.openOrders ? position.openOrders.filter(o => o.action === "Buy").map(o => o.quantity).reduce((tot, cur) => tot + cur, 0) : 0;
	const openSellCount = position.openOrders ? position.openOrders.filter(o => o.action === "Sell").map(o => o.quantity).reduce((tot, cur) => tot + cur, 0) : 0;
	const openOrderArray = [['B', openBuyCount], ['S', openSellCount]].filter(a => a[1] !== 0);
	const openOrderText = showAllPositions
		? openOrderArray.map(a => a.join(" ")).join(", ")
		: openOrderArray.filter(a => a[0] === action.charAt(0)).map(a => a[1]).join("");

	return (
		<tr key={position.symbol}>
			<td className="l">
				<a href={`https://finance.yahoo.com/quote/${position.symbol}`} target="_blank" style={position.peRatio < 0 ? { backgroundColor: "#FDD" } : {}}>{position.symbol}</a>
			</td>
			{showAllPositions && <td>{position.quantity}</td>}
			<td>{position.price.toFixed(2)}</td>
			{showAllPositions && <td>{position.marketValue.toFixed(2)}</td>}
			<td style={{ color: position.dayChangePct >= 0 ? "green" : "#C00" }}>{Math.abs(position.dayChangePct).toFixed(2) + "%"}</td>
			{showAllPositions && <td style={{ color: position.gainLossPct >= 0 ? "green" : "#C00" }}>{Math.abs(position.gainLossPct).toFixed(2) + "%"}</td>}
			{showAllPositions && <td>{position.percentOfAccount.toFixed(2) + "%"}</td>}
			{showAllPositions && <td>{position.targetPct ? position.targetPct.toFixed(2) + "%" : ""}</td>}
			{showAllPositions && <td>{position.targetPct ? (100 * position.percentOfAccount / position.targetPct).toFixed(1) + "%" : ""}</td>}
			<td className="l">{position.sharesToBuy ? (<a href={actionUrl} target="_blank" onClick={() => copyClip(Math.abs(position.sharesToBuy))}>{actionText}</a>) : ""}</td>
			<td className="c">
				{(openBuyCount || openSellCount) ? (
					<OverlayTrigger trigger={['hover', 'focus']} placement="right" overlay={renderOpenOrdersPopover(position, i)}>
						<span style={{ textDecoration: "underline" }}>{openOrderText}</span>
					</OverlayTrigger>
				) : ""}
			</td>
		</tr>
	);
}

function renderPositionPopover(position, i) {
	const low = position['52WeekLow'];
	const high = position['52WeekHigh'];
	const priceRangePct = 100 * (position.price - low) / (high - low);

	return (
		<Popover id={`popover-position-${i}`} style={{ maxWidth: 400 }}>
			<Popover.Header as="h3">{position.description}</Popover.Header>
			<Popover.Body>
				<table>
					<tbody>
						<tr><td className="l">52 week range:</td><td className="l">{`${low} - ${high} (${priceRangePct.toFixed(1)}%)`}</td></tr>
						{position.dividendYield && <tr><td className="l">Dividend yield:</td><td className="l">{`${position.dividendYield}%`}</td></tr>}
						{position.peRatio && <tr><td className="l">P/E ratio:</td><td className="l">{position.peRatio}</td></tr>}
					</tbody>
				</table>
			</Popover.Body>
		</Popover>
	);
}

function renderOpenOrdersPopover(position, i) {
	const sortedOrders = position.openOrders.sort((a, b) => b.limitPrice - a.limitPrice);
	const rows = [];
	rows.push(...sortedOrders.filter(o => o.action === 'Sell').map((order, j) => (
		<tr key={`row-opensell-${i}-${j}`}>
			<td>{order.action}</td>
			<td>{order.quantity}</td>
			<td>@</td>
			<td>{order.limitPrice.toFixed(2)}</td>
		</tr>
	)));
	rows.push((
		<tr key={`row-openorder-${i}-price`}>
			<td className="l" colSpan={3}>Current</td>
			<td>{position.price.toFixed(2)}</td>
		</tr>
	));
	rows.push(...sortedOrders.filter(o => o.action === 'Buy').map((order, j) => (
		<tr key={`row-openbuy-${i}-${j}`}>
			<td>{order.action}</td>
			<td>{order.quantity}</td>
			<td>@</td>
			<td>{order.limitPrice.toFixed(2)}</td>
		</tr>
	)));
	return (
		<Popover id={`popover-openorder-${i}`} style={{ maxWidth: 400 }}>
			<Popover.Header as="h3">Open Orders - {position.symbol}</Popover.Header>
			<Popover.Body>
				<table>
					<tbody>
						{rows}
					</tbody>
				</table>
			</Popover.Body>
		</Popover>
	);
}

function copyClip(text) {
	navigator.clipboard.writeText(text);
}

export default Positions;
